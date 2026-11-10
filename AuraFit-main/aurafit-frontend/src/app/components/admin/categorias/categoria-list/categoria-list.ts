import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { Categoria } from '../../../../models/categoria.model';
import { CategoriaService } from '../../../../services/categoria.service';
import { confirmar } from '../../../../shared/confirmacao-dialog';
import { mensagemDaApi } from '../../../../shared/erros-formulario';
import { NotificacaoService } from '../../../../shared/notificacao.service';

@Component({
  selector: 'app-categoria-list',
  imports: [
    RouterLink, MatTableModule, MatPaginatorModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatIconModule,
  ],
  templateUrl: './categoria-list.html',
})
export class CategoriaList implements OnInit {
  private categoriaService = inject(CategoriaService);
  private dialog = inject(MatDialog);
  private notificacao = inject(NotificacaoService);
  private temporizadorBusca?: ReturnType<typeof setTimeout>;

  colunas = ['nome', 'descricao', 'acoes'];

  categorias = signal<Categoria[]>([]);
  totalItems = signal(0);
  pageIndex = signal(0);
  pageSize = signal(10);
  filtro = signal('');
  carregando = signal(false);
  erro = signal('');

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando.set(true);
    this.erro.set('');

    this.categoriaService.listar(this.filtro(), this.pageIndex(), this.pageSize()).subscribe({
      next: (pagina) => {
        this.categorias.set(pagina.items);
        this.totalItems.set(pagina.totalItems);
        this.carregando.set(false);
      },
      error: (erro: HttpErrorResponse) => {
        this.erro.set(mensagemDaApi(erro, 'Não foi possível carregar as categorias.'));
        this.carregando.set(false);
      },
    });
  }

  buscar(texto: string): void {
    clearTimeout(this.temporizadorBusca);
    this.temporizadorBusca = setTimeout(() => {
      this.filtro.set(texto);
      this.pageIndex.set(0);
      this.carregar();
    }, 300);
  }

  mudarPagina(evento: PageEvent): void {
    this.pageIndex.set(evento.pageIndex);
    this.pageSize.set(evento.pageSize);
    this.carregar();
  }

  excluir(categoria: Categoria): void {
    confirmar(this.dialog, {
      titulo: 'Excluir categoria',
      mensagem: `Deseja excluir a categoria "${categoria.nome}"?`,
    }).subscribe((confirmou) => {
      if (!confirmou) return;

      this.categoriaService.excluir(categoria.id).subscribe({
        next: () => {
          this.notificacao.sucesso('Categoria excluída.');
          if (this.categorias().length === 1 && this.pageIndex() > 0) this.pageIndex.update((p) => p - 1);
          this.carregar();
        },
        error: (erro: HttpErrorResponse) =>
          this.notificacao.erro(mensagemDaApi(erro, 'Não foi possível excluir a categoria.')),
      });
    });
  }
}
