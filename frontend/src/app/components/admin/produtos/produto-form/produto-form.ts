import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { Observable, forkJoin } from 'rxjs';
import { Categoria } from '../../../../models/categoria.model';
import { Fornecedor } from '../../../../models/fornecedor.model';
import { Marca } from '../../../../models/marca.model';
import { ProdutoRequest } from '../../../../models/produto.model';
import { Sabor } from '../../../../models/sabor.model';
import { TipoProteina } from '../../../../models/tipo-proteina.model';
import { CategoriaService } from '../../../../services/categoria.service';
import { FornecedorService } from '../../../../services/fornecedor.service';
import { MarcaService } from '../../../../services/marca.service';
import { ProdutoService } from '../../../../services/produto.service';
import { SaborService } from '../../../../services/sabor.service';
import { aplicarErrosDaApi, mensagemErro } from '../../../../shared/erros-formulario';
import { NotificacaoService } from '../../../../shared/notificacao.service';
import { Validadores } from '../../../../shared/validadores';

@Component({
  selector: 'app-produto-form',
  imports: [
    CurrencyPipe,
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatSlideToggleModule,
  ],
  templateUrl: './produto-form.html',
})
export class ProdutoForm implements OnInit {
  private fb = inject(FormBuilder);
  private produtoService = inject(ProdutoService);
  private categoriaService = inject(CategoriaService);
  private marcaService = inject(MarcaService);
  private fornecedorService = inject(FornecedorService);
  private saborService = inject(SaborService);
  private notificacao = inject(NotificacaoService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  id = signal<number | null>(null);
  marcaOriginalId = signal<number | null>(null);
  carregando = signal(false);
  salvando = signal(false);
  erroGeral = signal('');

  tipos = signal<TipoProteina[]>([]);
  categorias = signal<Categoria[]>([]);
  marcas = signal<Marca[]>([]);
  fornecedores = signal<Fornecedor[]>([]);
  sabores = signal<Sabor[]>([]);

  form = this.fb.group({
    nome: this.fb.nonNullable.control('', [
      Validators.required,
      Validators.minLength(2),
      Validators.maxLength(120),
      Validadores.naoSomenteEspacos,
    ]),
    descricao: this.fb.nonNullable.control('', Validators.maxLength(1000)),
    preco: this.fb.control<number | null>(null, [Validators.required, Validators.min(0.01), Validadores.preco]),
    pesoGramas: this.fb.control<number | null>(null, [
      Validators.required,
      Validators.min(1),
      Validators.pattern(/^\d+$/),
    ]),
    estoque: this.fb.control<number | null>(null, [
      Validators.required,
      Validators.min(0),
      Validators.pattern(/^\d+$/),
    ]),
    idTipoProteina: this.fb.control<number | null>(null, Validators.required),
    idCategoria: this.fb.control<number | null>(null, Validators.required),
    idMarca: this.fb.control<number | null>(null, Validators.required),
    idFornecedor: this.fb.control<number | null>(null, Validators.required),
    idsSabores: this.fb.nonNullable.control<number[]>([]),
    ativo: this.fb.nonNullable.control(true),
  });

  mensagemErro = (campo: string) => mensagemErro(this.form.get(campo));

  ngOnInit(): void {
    forkJoin({
      tipos: this.produtoService.listarTiposProteina(),
      categorias: this.categoriaService.listarTodos(),
      marcas: this.marcaService.listarTodos(),
      fornecedores: this.fornecedorService.listarTodos(),
      sabores: this.saborService.listarTodos(),
    }).subscribe({
      next: (r) => {
        this.tipos.set(r.tipos);
        this.categorias.set(r.categorias);
        this.marcas.set(r.marcas);
        this.fornecedores.set(r.fornecedores);
        this.sabores.set(r.sabores);
      },
      error: () => this.erroGeral.set('Não foi possível carregar as opções do formulário. O back-end está rodando?'),
    });

    const idDaUrl = this.route.snapshot.paramMap.get('id');
    if (!idDaUrl) return;

    this.id.set(Number(idDaUrl));
    this.carregando.set(true);
    this.produtoService.buscarPorId(this.id()!).subscribe({
      next: (produto) => {
        this.marcaOriginalId.set(produto.marca.id);
        this.form.patchValue({
          nome: produto.nome,
          descricao: produto.descricao ?? '',
          preco: produto.preco,
          pesoGramas: produto.pesoGramas,
          estoque: produto.estoque,
          idTipoProteina: produto.tipoProteina.id,
          idCategoria: produto.categoria.id,
          idMarca: produto.marca.id,
          idFornecedor: produto.fornecedor.id,
          idsSabores: produto.sabores.map((s) => s.id),
          ativo: produto.ativo,
        });
        this.carregando.set(false);
      },
      error: () => {
        this.erroGeral.set('Produto não encontrado.');
        this.carregando.set(false);
      },
    });
  }

  alternarSaborChip(idSabor: number): void {
    const atuais = this.form.controls.idsSabores.value;
    if (atuais.includes(idSabor)) {
      this.form.controls.idsSabores.setValue(atuais.filter((id) => id !== idSabor));
    } else {
      this.form.controls.idsSabores.setValue([...atuais, idSabor]);
    }
  }

  saborSelecionado(idSabor: number): boolean {
    return this.form.controls.idsSabores.value.includes(idSabor);
  }

  nomeTipoSelecionado(): string {
    const id = this.form.controls.idTipoProteina.value;
    return this.tipos().find((t) => t.id === id)?.nome ?? 'Tipo de Proteína';
  }

  nomeCategoriaSelecionada(): string {
    const id = this.form.controls.idCategoria.value;
    return this.categorias().find((c) => c.id === id)?.nome ?? 'Categoria';
  }

  nomeMarcaSelecionada(): string {
    const id = this.form.controls.idMarca.value;
    return this.marcas().find((m) => m.id === id)?.nome ?? 'Marca Homologada';
  }

  saboresSelecionadosNomes(): string[] {
    const ids = this.form.controls.idsSabores.value;
    return this.sabores()
      .filter((s) => ids.includes(s.id))
      .map((s) => s.nome);
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.salvando.set(true);
    this.erroGeral.set('');

    const valores = this.form.getRawValue();
    const dados: ProdutoRequest = {
      nome: valores.nome.trim(),
      descricao: valores.descricao.trim(),
      preco: valores.preco!,
      pesoGramas: valores.pesoGramas!,
      estoque: valores.estoque!,
      idTipoProteina: valores.idTipoProteina!,
      idCategoria: valores.idCategoria!,
      idMarca: valores.idMarca!,
      idFornecedor: valores.idFornecedor!,
      idsSabores: valores.idsSabores,
      ativo: valores.ativo,
    };

    const requisicao: Observable<unknown> = this.id()
      ? this.produtoService.alterar(this.id()!, dados)
      : this.produtoService.incluir(dados);

    requisicao.subscribe({
      next: () => {
        this.notificacao.sucesso(this.id() ? 'Produto atualizado.' : 'Produto cadastrado.');
        this.router.navigateByUrl('/admin/produtos');
      },
      error: (erro: HttpErrorResponse) => {
        this.erroGeral.set(aplicarErrosDaApi(this.form, erro));
        this.salvando.set(false);
      },
    });
  }
}
