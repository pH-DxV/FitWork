import { Injectable, computed, signal } from '@angular/core';
import { Produto } from '../models/produto.model';

export interface ItemCarrinho {
  produto: Produto;
  sabor: string | null;
  quantidade: number;
}

export const VALOR_FRETE = 19.9;
export const FRETE_GRATIS_A_PARTIR = 199;

/**
 * Carrinho de compras da loja (protótipo): mantido em memória com signals
 * e salvo no localStorage para não se perder ao recarregar a página.
 */
@Injectable({ providedIn: 'root' })
export class CarrinhoService {
  private readonly chave = 'aurafit.carrinho';

  itens = signal<ItemCarrinho[]>(this.lerDoNavegador());

  quantidadeTotal = computed(() => this.itens().reduce((soma, item) => soma + item.quantidade, 0));
  subtotal = computed(() => this.itens().reduce((soma, item) => soma + item.produto.preco * item.quantidade, 0));
  frete = computed(() => (this.subtotal() === 0 || this.subtotal() >= FRETE_GRATIS_A_PARTIR ? 0 : VALOR_FRETE));
  total = computed(() => this.subtotal() + this.frete());

  adicionar(produto: Produto, sabor: string | null, quantidade = 1): void {
    const existente = this.itens().find((i) => i.produto.id === produto.id && i.sabor === sabor);
    if (existente) {
      this.alterarQuantidade(existente, existente.quantidade + quantidade);
      return;
    }
    this.atualizar([...this.itens(), { produto, sabor, quantidade }]);
  }

  alterarQuantidade(item: ItemCarrinho, quantidade: number): void {
    if (quantidade <= 0) {
      this.remover(item);
      return;
    }
    const limite = Math.max(item.produto.estoque, 1);
    this.atualizar(this.itens().map((i) => (i === item ? { ...i, quantidade: Math.min(quantidade, limite) } : i)));
  }

  remover(item: ItemCarrinho): void {
    this.atualizar(this.itens().filter((i) => i !== item));
  }

  limpar(): void {
    this.atualizar([]);
  }

  private atualizar(itens: ItemCarrinho[]): void {
    this.itens.set(itens);
    try {
      localStorage.setItem(this.chave, JSON.stringify(itens));
    } catch {
      // armazenamento indisponível: o carrinho continua funcionando em memória
    }
  }

  private lerDoNavegador(): ItemCarrinho[] {
    try {
      return JSON.parse(localStorage.getItem(this.chave) ?? '[]') as ItemCarrinho[];
    } catch {
      return [];
    }
  }
}
