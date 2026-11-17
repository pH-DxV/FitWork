import { Injectable, signal } from '@angular/core';
import { Endereco, Telefone } from '../models/cliente.model';

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
 * Sessão simulada da loja (protótipo de telas).
 * A autenticação real (Usuário + JWT) será implementada numa próxima etapa da disciplina.
 */
@Injectable({ providedIn: 'root' })
export class SessaoService {
  usuario = signal<UsuarioSessao | null>(null);
  /** Alias para compatibilidade nas telas B2B da loja */
  readonly cliente = this.usuario;

  pedidos = signal<PedidoResumo[]>([
    { numero: 'FW-10231', data: '2026-09-12', total: 259.8, itens: 2, formaPagamento: 'Pix', situacao: 'Entregue' },
  ]);

  entrar(email: string): void {
    const nome = email.split('@')[0].replace(/[._-]+/g, ' ');
    this.usuario.set({
      id: 1,
      nome: nome.replace(/\b\w/g, (letra) => letra.toUpperCase()),
      email,
      cpf: '123.456.789-00',
      dataNascimento: '1992-05-14',
      telefones: [{ codigoArea: '11', numero: '988887777', whatsapp: true }],
      endereco: {
        cep: '01310930',
        logradouro: 'Av. Paulista',
        numero: '2100',
        complemento: 'Conj. 142',
        bairro: 'Bela Vista',
        municipio: {
          id: 1,
          nome: 'São Paulo',
          codigoIbge: '3550308',
          estado: {
            id: 25,
            nome: 'São Paulo',
            sigla: 'SP',
            regiao: { id: 3, nome: 'Sudeste' },
          },
        },
      },
    });
  }

  sair(): void {
    this.usuario.set(null);
  }

  registrarPedido(pedido: PedidoResumo): void {
    this.pedidos.update((lista) => [pedido, ...lista]);
  }
}
