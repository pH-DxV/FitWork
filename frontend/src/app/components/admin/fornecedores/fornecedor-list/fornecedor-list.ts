import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { Observable } from 'rxjs';
import { Fornecedor, FornecedorRequest } from '../../../../models/fornecedor.model';
import { Produto } from '../../../../models/produto.model';
import { FornecedorService } from '../../../../services/fornecedor.service';
import { ProdutoService } from '../../../../services/produto.service';
import { confirmar } from '../../../../shared/confirmacao-dialog';
import { aplicarErrosDaApi, mensagemDaApi, mensagemErro } from '../../../../shared/erros-formulario';
import { FormatoPipe } from '../../../../shared/formato.pipe';
import { formatarCnpj, formatarTelefone, somenteNumeros } from '../../../../shared/formatos';
import { NotificacaoService } from '../../../../shared/notificacao.service';
import { Validadores } from '../../../../shared/validadores';

@Component({
  selector: 'app-fornecedor-list',
  imports: [
    RouterLink,
    ReactiveFormsModule,
    FormatoPipe,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
  ],
  templateUrl: './fornecedor-list.html',
})
export class FornecedorList implements OnInit {
  private fb = inject(FormBuilder);
  private fornecedorService = inject(FornecedorService);
  private produtoService = inject(ProdutoService);
  private dialog = inject(MatDialog);
  private notificacao = inject(NotificacaoService);
  private temporizadorBusca?: ReturnType<typeof setTimeout>;

  colunas = ['razaoSocial', 'cnpj', 'telefone', 'email', 'acoes'];

  fornecedores = signal<Fornecedor[]>([]);
  todosFornecedores = signal<Fornecedor[]>([]);
  produtos = signal<Produto[]>([]);
  totalItems = signal(0);
  pageIndex = signal(0);
  pageSize = signal(10);
  filtro = signal('');
  carregando = signal(false);
  erro = signal('');

  // Estado do formulário lateral integrado (Stitch 8/4)
  editandoId = signal<number | null>(null);
  salvando = signal(false);
  erroFormulario = signal('');

  form = this.fb.nonNullable.group({
    razaoSocial: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(120), Validadores.naoSomenteEspacos]],
    cnpj: ['', [Validators.required, Validadores.cnpj]],
    telefone: ['', [Validators.required, Validadores.telefone]],
    email: ['', [Validators.email, Validators.maxLength(100)]],
  });

  mensagemErro = (campo: string) => mensagemErro(this.form.get(campo));

  contagemPorFornecedor = computed(() => {
    const mapa = new Map<number, number>();
    for (const p of this.produtos()) {
      if (p.fornecedor?.id) {
        mapa.set(p.fornecedor.id, (mapa.get(p.fornecedor.id) ?? 0) + 1);
      }
    }
    return mapa;
  });

  participacaoFornecedores = computed(() => {
    const total = this.produtos().length || 1;
    const base = this.todosFornecedores().length > 0 ? this.todosFornecedores() : this.fornecedores();
    const cores = ['bg-[#2E7D32]', 'bg-[#00695C]', 'bg-amber-600', 'bg-blue-600'];
    return base
      .map((f, idx) => {
        const qtd = this.contagemPorFornecedor().get(f.id) ?? 0;
        const percentual = Math.round((qtd / total) * 100);
        return {
          fornecedor: f,
          qtd,
          percentual,
          corBarra: cores[idx % cores.length],
        };
      })
      .sort((a, b) => b.qtd - a.qtd)
      .slice(0, 4);
  });

  ngOnInit(): void {
    this.carregar();
    this.carregarMetricasCatalogo();
  }

  carregar(): void {
    this.carregando.set(true);
    this.erro.set('');

    this.fornecedorService.listar(this.filtro(), this.pageIndex(), this.pageSize()).subscribe({
      next: (pagina) => {
        this.fornecedores.set(pagina.items);
        this.totalItems.set(pagina.totalItems);
        this.carregando.set(false);
      },
      error: (erro: HttpErrorResponse) => {
        this.erro.set(mensagemDaApi(erro, 'Não foi possível carregar os fornecedores.'));
        this.carregando.set(false);
      },
    });
  }

  private carregarMetricasCatalogo(): void {
    this.fornecedorService.listarTodos().subscribe({
      next: (todos) => this.todosFornecedores.set(todos),
      error: () => {},
    });
    this.produtoService.listar({}, 0, 100).subscribe({
      next: (pag) => this.produtos.set(pag.items),
      error: () => {},
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

  qtdProdutos(fornecedorId: number): number {
    return this.contagemPorFornecedor().get(fornecedorId) ?? 0;
  }

  iniciaisFornecedor(razaoSocial: string): string {
    const partes = (razaoSocial || 'FN').trim().split(/\s+/);
    if (partes.length >= 2) {
      return (partes[0][0] + partes[1][0]).toUpperCase();
    }
    return razaoSocial.slice(0, 2).toUpperCase();
  }

  formatarCnpjInput(): void {
    const controle = this.form.controls.cnpj;
    if (somenteNumeros(controle.value).length === 14) {
      controle.setValue(formatarCnpj(controle.value));
    }
  }

  formatarTelefoneInput(): void {
    const controle = this.form.controls.telefone;
    const tamanho = somenteNumeros(controle.value).length;
    if (tamanho === 10 || tamanho === 11) {
      controle.setValue(formatarTelefone(controle.value));
    }
  }

  selecionarParaEdicao(fornecedor: Fornecedor): void {
    this.editandoId.set(fornecedor.id);
    this.erroFormulario.set('');
    this.form.reset({
      razaoSocial: fornecedor.razaoSocial,
      cnpj: formatarCnpj(fornecedor.cnpj),
      telefone: formatarTelefone(fornecedor.telefone),
      email: fornecedor.email ?? '',
    });
  }

  limparFormulario(): void {
    this.editandoId.set(null);
    this.erroFormulario.set('');
    this.form.reset({
      razaoSocial: '',
      cnpj: '',
      telefone: '',
      email: '',
    });
  }

  salvarInline(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.salvando.set(true);
    this.erroFormulario.set('');

    const valores = this.form.getRawValue();
    const dados: FornecedorRequest = {
      ...valores,
      razaoSocial: valores.razaoSocial.trim(),
      cnpj: somenteNumeros(valores.cnpj),
      telefone: somenteNumeros(valores.telefone),
      email: valores.email.trim(),
    };

    const idAtual = this.editandoId();
    const requisicao: Observable<unknown> = idAtual
      ? this.fornecedorService.alterar(idAtual, dados)
      : this.fornecedorService.incluir(dados);

    requisicao.subscribe({
      next: () => {
        this.notificacao.sucesso(idAtual ? 'Fornecedor atualizado.' : 'Fornecedor cadastrado.');
        this.salvando.set(false);
        this.limparFormulario();
        this.carregar();
        this.carregarMetricasCatalogo();
      },
      error: (erro: HttpErrorResponse) => {
        this.erroFormulario.set(aplicarErrosDaApi(this.form, erro));
        this.salvando.set(false);
      },
    });
  }

  excluir(fornecedor: Fornecedor): void {
    confirmar(this.dialog, {
      titulo: 'Excluir fornecedor',
      mensagem: `Deseja excluir o fornecedor "${fornecedor.razaoSocial}"?`,
    }).subscribe((confirmou) => {
      if (!confirmou) return;

      this.fornecedorService.excluir(fornecedor.id).subscribe({
        next: () => {
          this.notificacao.sucesso('Fornecedor excluído.');
          if (this.editandoId() === fornecedor.id) this.limparFormulario();
          if (this.fornecedores().length === 1 && this.pageIndex() > 0) this.pageIndex.update((p) => p - 1);
          this.carregar();
          this.carregarMetricasCatalogo();
        },
        error: (erro: HttpErrorResponse) =>
          this.notificacao.erro(mensagemDaApi(erro, 'Não foi possível excluir o fornecedor.')),
      });
    });
  }
}
