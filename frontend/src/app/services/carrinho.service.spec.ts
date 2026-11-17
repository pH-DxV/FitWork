import { beforeEach, describe, expect, it } from 'vitest';
import { Produto } from '../models/produto.model';
import { CarrinhoService, FRETE_GRATIS_A_PARTIR, VALOR_FRETE } from './carrinho.service';

describe('CarrinhoService', () => {
  let service: CarrinhoService;

  const produtoTeste = (id: number, preco: number, estoque = 5): Produto => ({
    id,
    nome: `Produto ${id}`,
    descricao: null,
    preco,
    pesoGramas: 900,
    estoque,
    tipoProteina: { id: 1, nome: 'Concentrado' },
    ativo: true,
    categoria: { id: 1, nome: 'Whey', descricao: null },
    marca: { id: 1, nome: 'Growth', cnpj: '11222333000181', emailContato: null, ativa: true },
    fornecedor: { id: 1, razaoSocial: 'Fit', cnpj: '11222333000181', telefone: '63999990001', email: null },
    sabores: [],
  });

  beforeEach(() => {
    localStorage.clear();
    service = new CarrinhoService();
  });

  it('deve cobrar frete abaixo de R$ 199 e conceder frete grátis a partir de R$ 199 (RN-019)', () => {
    service.adicionar(produtoTeste(1, 100), 'Chocolate', 1);
    expect(service.subtotal()).toBe(100);
    expect(service.frete()).toBe(VALOR_FRETE);
    expect(service.total()).toBe(100 + VALOR_FRETE);

    service.adicionar(produtoTeste(1, 100), 'Chocolate', 1);
    expect(service.subtotal()).toBe(200);
    expect(service.subtotal()).toBeGreaterThanOrEqual(FRETE_GRATIS_A_PARTIR);
    expect(service.frete()).toBe(0);
    expect(service.total()).toBe(200);
  });

  it('deve respeitar o limite de estoque ao incrementar quantidade (RN-019)', () => {
    const p = produtoTeste(2, 50, 3);
    service.adicionar(p, null, 10);
    // na primeira adição e em alterarQuantidade subsequente respeita o estoque
    service.alterarQuantidade(service.itens()[0], 10);
    expect(service.itens()[0].quantidade).toBe(3);
  });

  it('não deve adicionar produto com estoque zerado', () => {
    const semEstoque = produtoTeste(3, 80, 0);
    service.adicionar(semEstoque, null, 1);
    expect(service.itens().length).toBe(0);
    expect(service.quantidadeTotal()).toBe(0);
  });
});
