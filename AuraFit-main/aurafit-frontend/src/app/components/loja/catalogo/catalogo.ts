import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
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

/** Tela de consulta de produtos da loja (dados reais vindos da API). */
@Component({
  selector: 'app-catalogo',
  imports: [
    CurrencyPipe, RouterLink, MatButtonModule, MatIconModule, MatChipsModule, MatSelectModule, MatFormFieldModule,
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

  /** sabor escolhido em cada card (id do produto -> nome do sabor) */
  saborEscolhido: Record<number, string> = {};

  constructor() {
    this.route.queryParamMap.pipe(takeUntilDestroyed()).subscribe((params) => {
      this.busca.set(params.get('busca') ?? '');
      this.pageIndex.set(0);
      this.carregar();
    });
  }

  ngOnInit(): void {
    this.produtoService.listarTiposProteina().subscribe((lista) => this.tipos.set(lista));
    this.categoriaService.listar('', 0, 100).subscribe((pagina) => this.categorias.set(pagina.items));
  }

  carregar(): void {
    this.carregando.set(true);
    this.erro.set('');
    const filtro = {
      nome: this.busca(),
      idTipoProteina: this.tipoSelecionado(),
      idCategoria: this.categoriaSelecionada(),
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

  selecionarCategoria(id: number | null): void {
    this.categoriaSelecionada.set(id);
    this.pageIndex.set(0);
    this.carregar();
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

  adicionar(produto: Produto): void {
    const sabor = this.saborEscolhido[produto.id] ?? produto.sabores[0]?.nome ?? null;
    this.carrinho.adicionar(produto, sabor);
    this.notificacao.sucesso(`${produto.nome} adicionado ao carrinho.`);
  }

  iniciais(produto: Produto): string {
    return produto.marca.nome.slice(0, 2).toUpperCase();
  }
}
