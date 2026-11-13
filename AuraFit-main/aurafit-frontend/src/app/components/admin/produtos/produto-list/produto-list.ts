import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { Categoria } from '../../../../models/categoria.model';
import { Marca } from '../../../../models/marca.model';
import { Produto } from '../../../../models/produto.model';
import { TipoProteina } from '../../../../models/tipo-proteina.model';
import { CategoriaService } from '../../../../services/categoria.service';
import { MarcaService } from '../../../../services/marca.service';
import { ProdutoService } from '../../../../services/produto.service';
import { confirmar } from '../../../../shared/confirmacao-dialog';
import { mensagemDaApi } from '../../../../shared/erros-formulario';
import { NotificacaoService } from '../../../../shared/notificacao.service';

/**
 * Tela administrativa com relacionamento many-to-one (Produto -> Marca/Categoria/Fornecedor),
 * filtros por atributo e paginação feita no back-end.
 */
@Component({
  selector: 'app-produto-list',
  imports: [
    RouterLink, CurrencyPipe, MatTableModule, MatPaginatorModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatButtonModule, MatIconModule,
  ],
  templateUrl: './produto-list.html',
})
export class ProdutoList implements OnInit {
  private produtoService = inject(ProdutoService);
  private marcaService = inject(MarcaService);
  private categoriaService = inject(CategoriaService);
  private dialog = inject(MatDialog);
  private notificacao = inject(NotificacaoService);
  private temporizadorBusca?: ReturnType<typeof setTimeout>;

  colunas = ['nome', 'marca', 'categoria', 'tipo', 'preco', 'estoque', 'situacao', 'acoes'];

  produtos = signal<Produto[]>([]);
  totalItems = signal(0);
  pageIndex = signal(0);
  pageSize = signal(5);
  carregando = signal(false);
  erro = signal('');

  tipos = signal<TipoProteina[]>([]);
  categorias = signal<Categoria[]>([]);
  marcas = signal<Marca[]>([]);

  filtroNome = signal('');
  filtroTipo = signal<number | null>(null);
  filtroCategoria = signal<number | null>(null);
  filtroMarca = signal<number | null>(null);

  ngOnInit(): void {
    this.produtoService.listarTiposProteina().subscribe((lista) => this.tipos.set(lista));
    this.categoriaService.listarTodos().subscribe((lista) => this.categorias.set(lista));
    this.marcaService.listarTodos().subscribe((lista) => this.marcas.set(lista));
    this.carregar();
  }

  carregar(): void {
    this.carregando.set(true);
    this.erro.set('');

    const filtro = {
      nome: this.filtroNome(),
      idTipoProteina: this.filtroTipo(),
      idCategoria: this.filtroCategoria(),
      idMarca: this.filtroMarca(),
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

  buscarPorNome(texto: string): void {
    clearTimeout(this.temporizadorBusca);
    this.temporizadorBusca = setTimeout(() => {
      this.filtroNome.set(texto);
      this.recarregarDoInicio();
    }, 300);
  }

  filtrarPorTipo(id: number | null): void {
    this.filtroTipo.set(id);
    this.recarregarDoInicio();
  }

  filtrarPorCategoria(id: number | null): void {
    this.filtroCategoria.set(id);
    this.recarregarDoInicio();
  }

  filtrarPorMarca(id: number | null): void {
    this.filtroMarca.set(id);
    this.recarregarDoInicio();
  }

  limparFiltros(campoBusca: HTMLInputElement): void {
    campoBusca.value = '';
    this.filtroNome.set('');
    this.filtroTipo.set(null);
    this.filtroCategoria.set(null);
    this.filtroMarca.set(null);
    this.recarregarDoInicio();
  }

  temFiltro(): boolean {
    return !!(this.filtroNome() || this.filtroTipo() || this.filtroCategoria() || this.filtroMarca());
  }

  mudarPagina(evento: PageEvent): void {
    this.pageIndex.set(evento.pageIndex);
    this.pageSize.set(evento.pageSize);
    this.carregar();
  }

  excluir(produto: Produto): void {
    confirmar(this.dialog, {
      titulo: 'Excluir produto',
      mensagem: `Deseja excluir o produto "${produto.nome}"?`,
    }).subscribe((confirmou) => {
      if (!confirmou) return;

      this.produtoService.excluir(produto.id).subscribe({
        next: () => {
          this.notificacao.sucesso('Produto excluído.');
          if (this.produtos().length === 1 && this.pageIndex() > 0) this.pageIndex.update((p) => p - 1);
          this.carregar();
        },
        error: (erro: HttpErrorResponse) =>
          this.notificacao.erro(mensagemDaApi(erro, 'Não foi possível excluir o produto.')),
      });
    });
  }

  private recarregarDoInicio(): void {
    this.pageIndex.set(0);
    this.carregar();
  }
}
