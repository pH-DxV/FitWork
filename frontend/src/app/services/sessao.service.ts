import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, catchError, finalize, map, of, shareReplay, tap } from 'rxjs';
import { API_URL } from '../api.config';
import {
  AuthResponse,
  CadastroContaRequest,
  LoginRequest,
  PerfilUsuario,
  UsuarioAutenticado,
} from '../models/auth.model';
import { Cliente, Endereco, Telefone } from '../models/cliente.model';

export interface UsuarioSessao {
  id: number;
  nome: string;
  email: string;
  cpf: string;
  dataNascimento: string;
  telefones: Telefone[];
  endereco: Endereco;
}

export interface PedidoResumo {
  numero: string;
  data: string;
  total: number;
  itens: number;
  formaPagamento: string;
  situacao: string;
}

/**
 * Indicador não sensível (apenas "1") para saber se vale tentar restaurar a sessão
 * via cookie HttpOnly quando o usuário recarrega a aba (F5). Nenhum token ou dado
 * pessoal é gravado em localStorage/sessionStorage.
 */
const CHAVE_MARCADOR_SESSAO = 'fitwork.sessao.ativa';

const ENDERECO_INSTITUCIONAL_PADRAO: Endereco = {
  cep: '77001002',
  logradouro: 'Quadra 104 Norte, Av. Juscelino Kubitschek',
  numero: '100',
  complemento: 'Sede Administrativa FitWork',
  bairro: 'Plano Diretor Norte',
  municipio: {
    id: 1,
    nome: 'Palmas',
    codigoIbge: '1721000',
    estado: {
      id: 27,
      nome: 'Tocantins',
      sigla: 'TO',
      regiao: { id: 1, nome: 'Norte' },
    },
  },
};

/**
 * Gerencia o estado de autenticação no Angular intermediado exclusivamente pelo back-end Quarkus.
 * - O Angular NUNCA conversa diretamente com o Keycloak;
 * - O access token (curta duração) permanece apenas em memória (signal privado);
 * - O refresh token fica somente em cookie HttpOnly + SameSite=Strict gerenciado pelo Quarkus.
 */
@Injectable({ providedIn: 'root' })
export class SessaoService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_URL}/auth`;

  private readonly tokenAcesso = signal<string | null>(null);
  private readonly contaAutenticada = signal<UsuarioAutenticado | null>(null);
  private renovacaoEmAndamento$: Observable<string> | null = null;

  /** Dados completos da conta autenticada (Keycloak + papéis + cliente vinculado). */
  readonly conta = this.contaAutenticada.asReadonly();

  /** Cadastro de cliente vinculado à conta (nulo para administradores sem registro na tabela cliente). */
  readonly cadastroCliente = computed<Cliente | null>(() => this.contaAutenticada()?.cliente ?? null);

  readonly estaAutenticado = computed(
    () => this.tokenAcesso() !== null && this.contaAutenticada() !== null,
  );

  readonly perfis = computed<PerfilUsuario[]>(() => this.contaAutenticada()?.perfis ?? []);
  readonly ehAdmin = computed(() => this.perfis().includes('ADMIN'));
  readonly ehCliente = computed(() => this.perfis().includes('CLIENTE'));

  /**
   * Visão consolidada do usuário logado usada pelos cabeçalhos, checkout e portal do cliente.
   */
  readonly usuario = computed<UsuarioSessao | null>(() => {
    const conta = this.contaAutenticada();
    if (!conta) return null;

    const c = conta.cliente;
    if (c) {
      return {
        id: c.id,
        nome: c.nome,
        email: c.email,
        cpf: c.cpf,
        dataNascimento: c.dataNascimento,
        telefones: c.telefones,
        endereco: c.endereco,
      };
    }

    return {
      id: 0,
      nome: conta.nome || conta.email,
      email: conta.email,
      cpf: 'Credencial Administrativa',
      dataNascimento: '1990-01-01',
      telefones: [],
      endereco: ENDERECO_INSTITUCIONAL_PADRAO,
    };
  });

  /** Alias para compatibilidade com os templates da vitrine e perfil. */
  readonly cliente = this.usuario;

  pedidos = signal<PedidoResumo[]>([
    {
      numero: 'FW-10231',
      data: '2026-09-12',
      total: 259.8,
      itens: 2,
      formaPagamento: 'Pix',
      situacao: 'Entregue',
    },
  ]);

  constructor() {
    if (this.possuiMarcadorSessao()) {
      this.garantirSessaoRestaurada().subscribe();
    }
  }

  obterAccessToken(): string | null {
    return this.tokenAcesso();
  }

  possuiPerfil(perfil: PerfilUsuario): boolean {
    return this.perfis().includes(perfil);
  }

  entrar(credenciais: LoginRequest): Observable<UsuarioAutenticado> {
    return this.http
      .post<AuthResponse>(`${this.url}/login`, credenciais, { withCredentials: true })
      .pipe(
        tap((res) => this.aplicarSessao(res)),
        map((res) => res.usuario),
      );
  }

  cadastrar(dados: CadastroContaRequest): Observable<UsuarioAutenticado> {
    return this.http
      .post<AuthResponse>(`${this.url}/cadastro`, dados, { withCredentials: true })
      .pipe(
        tap((res) => this.aplicarSessao(res)),
        map((res) => res.usuario),
      );
  }

  carregarPerfil(): Observable<UsuarioAutenticado> {
    return this.http.get<UsuarioAutenticado>(`${this.url}/me`).pipe(
      tap((usuario) => this.contaAutenticada.set(usuario)),
    );
  }

  /**
   * Solicita ao Quarkus a renovação do access token usando o cookie HttpOnly (FITWORK_RT).
   * Requisições concorrentes compartilham a mesma chamada em voo.
   */
  renovarToken(): Observable<string> {
    if (this.renovacaoEmAndamento$) {
      return this.renovacaoEmAndamento$;
    }

    this.renovacaoEmAndamento$ = this.http
      .post<AuthResponse>(`${this.url}/refresh`, null, { withCredentials: true })
      .pipe(
        tap((res) => this.aplicarSessao(res)),
        map((res) => res.accessToken),
        catchError((erro) => {
          this.limparSessaoLocal();
          throw erro;
        }),
        finalize(() => {
          this.renovacaoEmAndamento$ = null;
        }),
        shareReplay(1),
      );

    return this.renovacaoEmAndamento$;
  }

  /**
   * Usado pelos Route Guards antes de decidir a navegação:
   * se o token em memória sumiu após um F5 mas há cookie HttpOnly ativo, restaura a sessão.
   */
  garantirSessaoRestaurada(): Observable<boolean> {
    if (this.estaAutenticado()) {
      return of(true);
    }
    if (!this.possuiMarcadorSessao()) {
      return of(false);
    }
    return this.renovarToken().pipe(
      map(() => true),
      catchError(() => of(false)),
    );
  }

  /**
   * Encerra a sessão no Keycloak (por meio do Quarkus), apaga o cookie HttpOnly
   * e limpa os dados locais em memória.
   */
  sair(): Observable<void> {
    return this.http.post<void>(`${this.url}/logout`, null, { withCredentials: true }).pipe(
      catchError(() => of(undefined)),
      finalize(() => this.limparSessaoLocal()),
    );
  }

  limparSessaoLocal(): void {
    this.tokenAcesso.set(null);
    this.contaAutenticada.set(null);
    this.definirMarcadorSessao(false);
  }

  atualizarClienteVinculado(cliente: Cliente): void {
    const atual = this.contaAutenticada();
    if (!atual) return;
    this.contaAutenticada.set({
      ...atual,
      nome: cliente.nome,
      email: cliente.email,
      cliente,
    });
  }

  registrarPedido(pedido: PedidoResumo): void {
    this.pedidos.update((lista) => [pedido, ...lista]);
  }

  private aplicarSessao(resposta: AuthResponse): void {
    this.tokenAcesso.set(resposta.accessToken);
    this.contaAutenticada.set(resposta.usuario);
    this.definirMarcadorSessao(true);
  }

  private possuiMarcadorSessao(): boolean {
    try {
      return typeof sessionStorage !== 'undefined' && sessionStorage.getItem(CHAVE_MARCADOR_SESSAO) === '1';
    } catch {
      return false;
    }
  }

  private definirMarcadorSessao(ativo: boolean): void {
    try {
      if (typeof sessionStorage === 'undefined') return;
      if (ativo) {
        sessionStorage.setItem(CHAVE_MARCADOR_SESSAO, '1');
      } else {
        sessionStorage.removeItem(CHAVE_MARCADOR_SESSAO);
      }
    } catch {
      // ignora ambientes sem sessionStorage
    }
  }
}
