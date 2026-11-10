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
import { Cliente } from '../../../../models/cliente.model';
import { ClienteService } from '../../../../services/cliente.service';
import { confirmar } from '../../../../shared/confirmacao-dialog';
import { mensagemDaApi } from '../../../../shared/erros-formulario';
import { FormatoPipe } from '../../../../shared/formato.pipe';
import { NotificacaoService } from '../../../../shared/notificacao.service';

@Component({
  selector: 'app-cliente-list',
  imports: [
    RouterLink, FormatoPipe, MatTableModule, MatPaginatorModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatIconModule,
  ],
  templateUrl: './cliente-list.html',
})
export class ClienteList implements OnInit {
  private clienteService = inject(ClienteService);
  private dialog = inject(MatDialog);
  private notificacao = inject(NotificacaoService);
  private temporizadorBusca?: ReturnType<typeof setTimeout>;

  colunas = ['nome', 'cpf', 'telefone', 'cidade', 'situacao', 'acoes'];

  clientes = signal<Cliente[]>([]);
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

    this.clienteService.listar(this.filtro(), this.pageIndex(), this.pageSize()).subscribe({
      next: (pagina) => {
        this.clientes.set(pagina.items);
        this.totalItems.set(pagina.totalItems);
        this.carregando.set(false);
      },
      error: (erro: HttpErrorResponse) => {
        this.erro.set(mensagemDaApi(erro, 'Não foi possível carregar os clientes.'));
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

  telefonePrincipal(cliente: Cliente): string {
    const telefone = cliente.telefones[0];
    return telefone ? telefone.codigoArea + telefone.numero : '';
  }

  excluir(cliente: Cliente): void {
    confirmar(this.dialog, {
      titulo: 'Excluir cliente',
      mensagem: `Deseja excluir o cliente "${cliente.nome}"? Os telefones e o endereço também serão removidos.`,
    }).subscribe((confirmou) => {
      if (!confirmou) return;

      this.clienteService.excluir(cliente.id).subscribe({
        next: () => {
          this.notificacao.sucesso('Cliente excluído.');
          if (this.clientes().length === 1 && this.pageIndex() > 0) this.pageIndex.update((p) => p - 1);
          this.carregar();
        },
        error: (erro: HttpErrorResponse) =>
          this.notificacao.erro(mensagemDaApi(erro, 'Não foi possível excluir o cliente.')),
      });
    });
  }
}
