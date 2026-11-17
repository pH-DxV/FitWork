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
import { Categoria, CategoriaRequest } from '../../../../models/categoria.model';
import { Produto } from '../../../../models/produto.model';
import { CategoriaService } from '../../../../services/categoria.service';
import { ProdutoService } from '../../../../services/produto.service';
import { confirmar } from '../../../../shared/confirmacao-dialog';
import { aplicarErrosDaApi, mensagemDaApi, mensagemErro } from '../../../../shared/erros-formulario';
import { NotificacaoService } from '../../../../shared/notificacao.service';
import { Validadores } from '../../../../shared/validadores';

@Component({
  selector: 'app-categoria-list',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
  ],
  templateUrl: './categoria-list.html',
})
export class CategoriaList implements OnInit {
  private fb = inject(FormBuilder);
  private categoriaService = inject(CategoriaService);
  private produtoService = inject(ProdutoService);
  private dialog = inject(MatDialog);
  private notificacao = inject(NotificacaoService);
  private temporizadorBusca?: ReturnType<typeof setTimeout>;

  colunas = ['codigo', 'nome', 'produtos', 'status', 'acoes'];

  categorias = signal<Categoria[]>([]);
  produtos = signal<Produto[]>([]);
  totalItems = signal(0);
  pageIndex = signal(0);
  pageSize = signal(10);
  filtro = signal('');
  carregando = signal(false);
  erro = signal('');

  // Formulário Lateral Integrado (Padrão Stitch 08)
  idEdicao = signal<number | null>(null);
  salvandoForm = signal(false);
  erroForm = signal('');

  form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100), Validadores.naoSomenteEspacos]],
    descricao: ['', [Validators.maxLength(255)]],
  });

  mensagemErro = (campo: string) => mensagemErro(this.form.get(campo));

  proporcaoCatalogo = computed(() => {
    const prods = this.produtos();
    const total = prods.length || 1;
    return this.categorias()
      .slice(0, 4)
      .map((cat) => {
        const qtd = prods.filter((p) => p.categoria?.id === cat.id).length;
        return {
          nome: cat.nome,
          quantidade: qtd,
          percentual: +((qtd / total) * 100).toFixed(1),
        };
      });
  });

  ngOnInit(): void {
    this.carregar();
    this.carregarProdutos();
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

  carregarProdutos(): void {
    this.produtoService.listar({}, 0, 100).subscribe({
      next: (pagina) => this.produtos.set(pagina.items),
    });
  }

  contarProdutos(idCategoria: number): number {
    return this.produtos().filter((p) => p.categoria?.id === idCategoria).length;
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

  selecionarParaEdicao(categoria: Categoria): void {
    this.idEdicao.set(categoria.id);
    this.erroForm.set('');
    this.form.patchValue({
      nome: categoria.nome,
      descricao: categoria.descricao ?? '',
    });
  }

  limparFormulario(): void {
    this.idEdicao.set(null);
    this.erroForm.set('');
    this.form.reset({ nome: '', descricao: '' });
  }

  salvarInline(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.salvandoForm.set(true);
    this.erroForm.set('');
    const valores = this.form.getRawValue();
    const dados: CategoriaRequest = {
      nome: valores.nome.trim(),
      descricao: valores.descricao.trim(),
    };

    const req: Observable<Categoria> = this.idEdicao()
      ? this.categoriaService.alterar(this.idEdicao()!, dados)
      : this.categoriaService.incluir(dados);

    req.subscribe({
      next: () => {
        this.notificacao.sucesso(this.idEdicao() ? 'Categoria atualizada.' : 'Categoria cadastrada.');
        this.salvandoForm.set(false);
        this.limparFormulario();
        this.carregar();
      },
      error: (erro: HttpErrorResponse) => {
        this.erroForm.set(aplicarErrosDaApi(this.form, erro));
        this.salvandoForm.set(false);
      },
    });
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
          if (this.idEdicao() === categoria.id) this.limparFormulario();
          if (this.categorias().length === 1 && this.pageIndex() > 0) this.pageIndex.update((p) => p - 1);
          this.carregar();
        },
        error: (erro: HttpErrorResponse) =>
          this.notificacao.erro(mensagemDaApi(erro, 'Não foi possível excluir a categoria.')),
      });
    });
  }
}
