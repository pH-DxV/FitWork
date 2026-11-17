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
import { Marca, MarcaRequest } from '../../../../models/marca.model';
import { Produto } from '../../../../models/produto.model';
import { MarcaService } from '../../../../services/marca.service';
import { ProdutoService } from '../../../../services/produto.service';
import { confirmar } from '../../../../shared/confirmacao-dialog';
import { aplicarErrosDaApi, mensagemDaApi, mensagemErro } from '../../../../shared/erros-formulario';
import { FormatoPipe } from '../../../../shared/formato.pipe';
import { formatarCnpj, somenteNumeros } from '../../../../shared/formatos';
import { NotificacaoService } from '../../../../shared/notificacao.service';
import { Validadores } from '../../../../shared/validadores';

@Component({
  selector: 'app-marca-list',
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
  templateUrl: './marca-list.html',
})
export class MarcaList implements OnInit {
  private fb = inject(FormBuilder);
  private marcaService = inject(MarcaService);
  private produtoService = inject(ProdutoService);
  private dialog = inject(MatDialog);
  private notificacao = inject(NotificacaoService);
  private temporizadorBusca?: ReturnType<typeof setTimeout>;

  colunas = ['nome', 'cnpj', 'email', 'situacao', 'acoes'];

  marcas = signal<Marca[]>([]);
  todasMarcas = signal<Marca[]>([]);
  produtos = signal<Produto[]>([]);
  totalItems = signal(0);
  pageIndex = signal(0);
  pageSize = signal(10);
  filtro = signal('');
  filtroStatus = signal<'TODAS' | 'ATIVAS' | 'INATIVAS'>('TODAS');
  carregando = signal(false);
  erro = signal('');

  // Estado do formulário lateral integrado (padrão Stitch 8/4)
  editandoId = signal<number | null>(null);
  salvando = signal(false);
  erroFormulario = signal('');

  form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100), Validadores.naoSomenteEspacos]],
    cnpj: ['', [Validators.required, Validadores.cnpj]],
    emailContato: ['', [Validators.email, Validators.maxLength(100)]],
    ativa: [true],
  });

  mensagemErro = (campo: string) => mensagemErro(this.form.get(campo));

  marcasExibidas = computed(() => {
    const status = this.filtroStatus();
    const lista = this.marcas();
    if (status === 'ATIVAS') return lista.filter((m) => m.ativa);
    if (status === 'INATIVAS') return lista.filter((m) => !m.ativa);
    return lista;
  });

  marcasAtivasCount = computed(() => {
    const base = this.todasMarcas().length > 0 ? this.todasMarcas() : this.marcas();
    return base.filter((m) => m.ativa).length;
  });

  marcasInativasCount = computed(() => {
    const base = this.todasMarcas().length > 0 ? this.todasMarcas() : this.marcas();
    return base.filter((m) => !m.ativa).length;
  });

  contagemPorMarca = computed(() => {
    const mapa = new Map<number, number>();
    for (const p of this.produtos()) {
      if (p.marca?.id) {
        mapa.set(p.marca.id, (mapa.get(p.marca.id) ?? 0) + 1);
      }
    }
    return mapa;
  });

  participacaoMarcas = computed(() => {
    const total = this.produtos().length || 1;
    const base = this.todasMarcas().length > 0 ? this.todasMarcas() : this.marcas();
    const cores = ['bg-[#2E7D32]', 'bg-[#00695C]', 'bg-amber-600', 'bg-blue-600', 'bg-purple-600'];
    return base
      .map((m, idx) => {
        const qtd = this.contagemPorMarca().get(m.id) ?? 0;
        const percentual = Math.round((qtd / total) * 100);
        return {
          marca: m,
          qtd,
          percentual,
          corBarra: m.ativa ? cores[idx % cores.length] : 'bg-slate-400',
        };
      })
      .sort((a, b) => b.qtd - a.qtd)
      .slice(0, 5);
  });

  ngOnInit(): void {
    this.carregar();
    this.carregarMetricasCatalogo();
  }

  carregar(): void {
    this.carregando.set(true);
    this.erro.set('');

    this.marcaService.listar(this.filtro(), this.pageIndex(), this.pageSize()).subscribe({
      next: (pagina) => {
        this.marcas.set(pagina.items);
        this.totalItems.set(pagina.totalItems);
        this.carregando.set(false);
      },
      error: (erro: HttpErrorResponse) => {
        this.erro.set(mensagemDaApi(erro, 'Não foi possível carregar as marcas.'));
        this.carregando.set(false);
      },
    });
  }

  private carregarMetricasCatalogo(): void {
    this.marcaService.listarTodos().subscribe({
      next: (todas) => this.todasMarcas.set(todas),
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

  selecionarFiltroStatus(status: 'TODAS' | 'ATIVAS' | 'INATIVAS'): void {
    this.filtroStatus.set(status);
  }

  mudarPagina(evento: PageEvent): void {
    this.pageIndex.set(evento.pageIndex);
    this.pageSize.set(evento.pageSize);
    this.carregar();
  }

  qtdProdutos(marcaId: number): number {
    return this.contagemPorMarca().get(marcaId) ?? 0;
  }

  iniciaisMarca(nome: string): string {
    const partes = (nome || 'AF').trim().split(/\s+/);
    if (partes.length >= 2) {
      return (partes[0][0] + partes[1][0]).toUpperCase();
    }
    return nome.slice(0, 2).toUpperCase();
  }

  formatarCnpjInput(): void {
    const controle = this.form.controls.cnpj;
    if (somenteNumeros(controle.value).length === 14) {
      controle.setValue(formatarCnpj(controle.value));
    }
  }

  selecionarParaEdicao(marca: Marca): void {
    this.editandoId.set(marca.id);
    this.erroFormulario.set('');
    this.form.reset({
      nome: marca.nome,
      cnpj: formatarCnpj(marca.cnpj),
      emailContato: marca.emailContato ?? '',
      ativa: marca.ativa,
    });
  }

  limparFormulario(): void {
    this.editandoId.set(null);
    this.erroFormulario.set('');
    this.form.reset({
      nome: '',
      cnpj: '',
      emailContato: '',
      ativa: true,
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
    const dados: MarcaRequest = {
      ...valores,
      nome: valores.nome.trim(),
      cnpj: somenteNumeros(valores.cnpj),
      emailContato: valores.emailContato.trim(),
    };

    const idAtual = this.editandoId();
    const requisicao: Observable<unknown> = idAtual
      ? this.marcaService.alterar(idAtual, dados)
      : this.marcaService.incluir(dados);

    requisicao.subscribe({
      next: () => {
        this.notificacao.sucesso(idAtual ? 'Marca atualizada.' : 'Marca cadastrada.');
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

  excluir(marca: Marca): void {
    confirmar(this.dialog, {
      titulo: 'Excluir marca',
      mensagem: `Deseja excluir a marca "${marca.nome}"?`,
    }).subscribe((confirmou) => {
      if (!confirmou) return;

      this.marcaService.excluir(marca.id).subscribe({
        next: () => {
          this.notificacao.sucesso('Marca excluída.');
          if (this.editandoId() === marca.id) this.limparFormulario();
          if (this.marcas().length === 1 && this.pageIndex() > 0) this.pageIndex.update((p) => p - 1);
          this.carregar();
          this.carregarMetricasCatalogo();
        },
        error: (erro: HttpErrorResponse) =>
          this.notificacao.erro(mensagemDaApi(erro, 'Não foi possível excluir a marca.')),
      });
    });
  }
}
