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
import { Sabor } from '../../../../models/sabor.model';
import { SaborService } from '../../../../services/sabor.service';
import { confirmar } from '../../../../shared/confirmacao-dialog';
import { mensagemDaApi } from '../../../../shared/erros-formulario';
import { NotificacaoService } from '../../../../shared/notificacao.service';

@Component({
  selector: 'app-sabor-list',
  imports: [
    RouterLink, MatTableModule, MatPaginatorModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatIconModule,
  ],
  templateUrl: './sabor-list.html',
})
export class SaborList implements OnInit {
  private saborService = inject(SaborService);
  private dialog = inject(MatDialog);
  private notificacao = inject(NotificacaoService);
  private temporizadorBusca?: ReturnType<typeof setTimeout>;

  colunas = ['nome', 'descricao', 'acoes'];

  sabores = signal<Sabor[]>([]);
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

    this.saborService.listar(this.filtro(), this.pageIndex(), this.pageSize()).subscribe({
      next: (pagina) => {
        this.sabores.set(pagina.items);
        this.totalItems.set(pagina.totalItems);
        this.carregando.set(false);
      },
      error: (erro: HttpErrorResponse) => {
        this.erro.set(mensagemDaApi(erro, 'Não foi possível carregar os sabores.'));
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

  excluir(sabor: Sabor): void {
    confirmar(this.dialog, {
      titulo: 'Excluir sabor',
      mensagem: `Deseja excluir o sabor "${sabor.nome}"?`,
    }).subscribe((confirmou) => {
      if (!confirmou) return;

      this.saborService.excluir(sabor.id).subscribe({
        next: () => {
          this.notificacao.sucesso('Sabor excluído.');
          if (this.sabores().length === 1 && this.pageIndex() > 0) this.pageIndex.update((p) => p - 1);
          this.carregar();
        },
        error: (erro: HttpErrorResponse) =>
          this.notificacao.erro(mensagemDaApi(erro, 'Não foi possível excluir o sabor.')),
      });
    });
  }
}
