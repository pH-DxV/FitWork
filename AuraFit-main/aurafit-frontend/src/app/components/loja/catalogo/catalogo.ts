import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { Categoria } from '../../../models/categoria.model';
import { Produto } from '../../../models/produto.model';
import { TipoProteina } from '../../../models/tipo-proteina.model';
import { CarrinhoService } from '../../../services/carrinho.service';
import { CategoriaService } from '../../../services/categoria.service';
import { ProdutoService } from '../../../services/produto.service';
import { mensagemDaApi } from '../../../shared/erros-formulario';
import { NotificacaoService } from '../../../shared/notificacao.service';

export type OrdemCatalogo = 'destaques' | 'menor_preco' | 'maior_preco' | 'nome_az';

/** Tela de consulta de produtos da loja (dados reais vindos da API + Design System Stitch). */
@Component({
  selector: 'app-catalogo',
  imports: [
    CurrencyPipe,
    RouterLink,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatSelectModule,
    MatFormFieldModule,
    MatPaginatorModule,
  ],
  templateUrl: './catalogo.html',
  styleUrl: './catalogo.css',
})
export class Catalogo implements OnInit {
  private produtoService = inject(ProdutoService);
  private categoriaService = inject(CategoriaService);
  private carrinho = inject(CarrinhoService);
  private notificacao = inject(NotificacaoService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  produtos = signal<Produto[]>([]);
  tipos = signal<TipoProteina[]>([]);
  categorias = signal<Categoria[]>([]);
  totalItems = signal(0);
  pageIndex = signal(0);
  pageSize = signal(8);
  carregando = signal(false);
  erro = signal('');

  busca = signal('');
  tipoSelecionado = signal<number | null>(null);
  categoriaSelecionada = signal<number | null>(null);
  ordenacao = signal<OrdemCatalogo>('destaques');
  favoritos = signal<Record<number, boolean>>({});

  /** sabor escolhido em cada card (id do produto -> nome do sabor) */
  saborEscolhido: Record<number, string> = {};

  produtosOrdenados = computed(() => {
    const lista = [...this.produtos()];
    const ordem = this.ordenacao();
    if (ordem === 'menor_preco') {
      return lista.sort((a, b) => a.preco - b.preco);
    }
    if (ordem === 'maior_preco') {
      return lista.sort((a, b) => b.preco - a.preco);
    }
    if (ordem === 'nome_az') {
      return lista.sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'));
    }
    return lista;
  });

  constructor() {
    this.route.queryParamMap.pipe(takeUntilDestroyed()).subscribe((params) => {
      this.busca.set(params.get('busca') ?? '');
      this.pageIndex.set(0);
      this.carregar();
    });
  }

  ngOnInit(): void {
    this.produtoService.listarTiposProteina().subscribe((lista) => this.tipos.set(lista));
    this.categoriaService.listarTodos().subscribe((lista) => this.categorias.set(lista));
  }

  carregar(): void {
    this.carregando.set(true);
    this.erro.set('');
    const filtro = {
      nome: this.busca(),
      idTipoProteina: this.tipoSelecionado(),
      idCategoria: this.categoriaSelecionada(),
      ativo: true,
    };
    this.produtoService.listar(filtro, this.pageIndex(), this.pageSize()).subscribe({
      next: (pagina) => {
        this.produtos.set(pagina.items);
        this.totalItems.set(pagina.totalItems);
        this.carregando.set(false);
      },
      error: (erro: HttpErrorResponse) => {
        this.erro.set(mensagemDaApi(erro, 'Não foi possível carregar os produtos.'));
        this.carregando.set(false);
      },
    });
  }

  selecionarTipo(id: number | null): void {
    this.tipoSelecionado.set(this.tipoSelecionado() === id ? null : id);
    this.pageIndex.set(0);
    this.carregar();
  }

  selecionarCategoria(valor: string): void {
    const id = valor ? Number(valor) : null;
    this.categoriaSelecionada.set(id);
    this.pageIndex.set(0);
    this.carregar();
  }

  alterarOrdenacao(valor: string): void {
    this.ordenacao.set((valor as OrdemCatalogo) || 'destaques');
  }

  limparFiltros(): void {
    this.tipoSelecionado.set(null);
    this.categoriaSelecionada.set(null);
    this.ordenacao.set('destaques');
    this.pageIndex.set(0);
    if (this.busca()) {
      this.router.navigate(['/loja'], { queryParams: { busca: null } });
    } else {
      this.carregar();
    }
  }

  mudarPagina(evento: PageEvent): void {
    this.pageIndex.set(evento.pageIndex);
    this.pageSize.set(evento.pageSize);
    this.carregar();
  }

  disponivel(produto: Produto): boolean {
    return produto.ativo && produto.estoque > 0;
  }

  escolherSabor(produto: Produto, sabor: string): void {
    this.saborEscolhido[produto.id] = sabor;
  }

  saborAtual(produto: Produto): string {
    return this.saborEscolhido[produto.id] ?? produto.sabores[0]?.nome ?? '';
  }

  alternarFavorito(idProduto: number): void {
    this.favoritos.update((atual) => ({ ...atual, [idProduto]: !atual[idProduto] }));
  }

  ehFavorito(idProduto: number): boolean {
    return !!this.favoritos()[idProduto];
  }

  adicionar(produto: Produto): void {
    const sabor = this.saborEscolhido[produto.id] ?? produto.sabores[0]?.nome ?? null;
    this.carrinho.adicionar(produto, sabor);
    this.notificacao.sucesso(`${produto.nome} adicionado ao pedido B2B.`);
  }

  avisarDisponibilidade(produto: Produto): void {
    this.notificacao.sucesso(`Você será notificado assim que novo lote de ${produto.nome} for liberado.`);
  }

  iniciais(produto: Produto): string {
    return produto.marca.nome.slice(0, 2).toUpperCase();
  }

  formatarPeso(gramas: number): string {
    if (gramas >= 1000) {
      const kg = gramas / 1000;
      return `${Number.isInteger(kg) ? kg : kg.toFixed(2).replace('.', ',')}kg`;
    }
    return `${gramas}g`;
  }
}
