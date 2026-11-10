import { Injectable, signal } from '@angular/core';

export interface UsuarioSessao {
  nome: string;
  email: string;
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
  pedidos = signal<PedidoResumo[]>([
    { numero: 'AF-10231', data: '2026-09-12', total: 259.8, itens: 2, formaPagamento: 'Pix', situacao: 'Entregue' },
  ]);

  entrar(email: string): void {
    const nome = email.split('@')[0].replace(/[._-]+/g, ' ');
    this.usuario.set({ nome: nome.replace(/\b\w/g, (letra) => letra.toUpperCase()), email });
  }

  sair(): void {
    this.usuario.set(null);
  }

  registrarPedido(pedido: PedidoResumo): void {
    this.pedidos.update((lista) => [pedido, ...lista]);
  }
}
