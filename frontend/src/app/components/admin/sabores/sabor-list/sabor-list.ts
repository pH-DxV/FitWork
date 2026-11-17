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
import { Produto } from '../../../../models/produto.model';
import { Sabor, SaborRequest } from '../../../../models/sabor.model';
import { ProdutoService } from '../../../../services/produto.service';
import { SaborService } from '../../../../services/sabor.service';
import { confirmar } from '../../../../shared/confirmacao-dialog';
import { aplicarErrosDaApi, mensagemDaApi, mensagemErro } from '../../../../shared/erros-formulario';
import { NotificacaoService } from '../../../../shared/notificacao.service';
import { Validadores } from '../../../../shared/validadores';

interface PaletaSabor {
  hex: string;
  borda: string;
  texto: string;
}

@Component({
  selector: 'app-sabor-list',
  imports: [
    RouterLink,
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
  ],
  templateUrl: './sabor-list.html',
})
export class SaborList implements OnInit {
  private fb = inject(FormBuilder);
  private saborService = inject(SaborService);
  private produtoService = inject(ProdutoService);
  private dialog = inject(MatDialog);
  private notificacao = inject(NotificacaoService);
  private temporizadorBusca?: ReturnType<typeof setTimeout>;

  colunas = ['nome', 'descricao', 'acoes'];

  sabores = signal<Sabor[]>([]);
  todosSabores = signal<Sabor[]>([]);
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
  corSelecionada = signal('#FDF6E2');

  readonly coresSugeridas = ['#FDF6E2', '#5D4037', '#F8BBD0', '#FFE0B2', '#DCEDC8', '#E0F2F1'];

  form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(80), Validadores.naoSomenteEspacos]],
    descricao: ['', [Validators.maxLength(255)]],
  });

  mensagemErro = (campo: string) => mensagemErro(this.form.get(campo));

  contagemPorSabor = computed(() => {
    const mapa = new Map<number, number>();
    for (const p of this.produtos()) {
      for (const s of p.sabores ?? []) {
        mapa.set(s.id, (mapa.get(s.id) ?? 0) + 1);
      }
    }
    return mapa;
  });

  saborLider = computed(() => {
    const base = this.todosSabores().length > 0 ? this.todosSabores() : this.sabores();
    if (base.length === 0) return null;
    let lider = base[0];
    let max = -1;
    for (const s of base) {
      const qtd = this.contagemPorSabor().get(s.id) ?? 0;
      if (qtd > max) {
        max = qtd;
        lider = s;
      }
    }
    return { sabor: lider, qtd: Math.max(0, max) };
  });

  preferenciaCatalogo = computed(() => {
    const base = this.todosSabores().length > 0 ? this.todosSabores() : this.sabores();
    const totalVinculos = Array.from(this.contagemPorSabor().values()).reduce((a, b) => a + b, 0) || 1;
    const cores = ['bg-amber-700', 'bg-amber-400', 'bg-pink-500', 'bg-orange-500', 'bg-[#2E7D32]'];
    return base
      .map((s, idx) => {
        const qtd = this.contagemPorSabor().get(s.id) ?? 0;
        const percentual = Math.round((qtd / totalVinculos) * 100);
        return {
          sabor: s,
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

  private carregarMetricasCatalogo(): void {
    this.saborService.listarTodos().subscribe({
      next: (todos) => this.todosSabores.set(todos),
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

  qtdProdutos(saborId: number): number {
    return this.contagemPorSabor().get(saborId) ?? 0;
  }

  paletaVisual(sabor: Sabor): PaletaSabor {
    const n = (sabor.nome || '').toLowerCase();
    if (n.includes('baunilha') || n.includes('vanilla')) {
      return { hex: '#FDF6E2', borda: 'border-amber-300', texto: 'text-amber-900' };
    }
    if (n.includes('chocolate') || n.includes('cacau') || n.includes('avelã')) {
      return { hex: '#5D4037', borda: 'border-amber-900/30', texto: 'text-amber-950' };
    }
    if (n.includes('morango') || n.includes('frutas vermelhas') || n.includes('berry')) {
      return { hex: '#F8BBD0', borda: 'border-pink-300', texto: 'text-pink-900' };
    }
    if (n.includes('cookies') || n.includes('caramelo') || n.includes('doce de leite')) {
      return { hex: '#D7CCC8', borda: 'border-stone-400', texto: 'text-stone-800' };
    }
    if (n.includes('limão') || n.includes('maçã') || n.includes('verde') || n.includes('citrus')) {
      return { hex: '#DCEDC8', borda: 'border-lime-400', texto: 'text-lime-900' };
    }
    if (n.includes('neutro') || n.includes('sem sabor') || n.includes('natural')) {
      return { hex: '#F1F5F9', borda: 'border-slate-300', texto: 'text-slate-700' };
    }
    return { hex: '#FFE0B2', borda: 'border-orange-300', texto: 'text-orange-900' };
  }

  selecionarParaEdicao(sabor: Sabor): void {
    this.editandoId.set(sabor.id);
    this.erroFormulario.set('');
    this.corSelecionada.set(this.paletaVisual(sabor).hex);
    this.form.reset({
      nome: sabor.nome,
      descricao: sabor.descricao ?? '',
    });
  }

  limparFormulario(): void {
    this.editandoId.set(null);
    this.erroFormulario.set('');
    this.corSelecionada.set('#FDF6E2');
    this.form.reset({
      nome: '',
      descricao: '',
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
    const dados: SaborRequest = {
      nome: valores.nome.trim(),
      descricao: valores.descricao.trim(),
    };

    const idAtual = this.editandoId();
    const requisicao: Observable<unknown> = idAtual
      ? this.saborService.alterar(idAtual, dados)
      : this.saborService.incluir(dados);

    requisicao.subscribe({
      next: () => {
        this.notificacao.sucesso(idAtual ? 'Sabor atualizado.' : 'Sabor cadastrado.');
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

  excluir(sabor: Sabor): void {
    confirmar(this.dialog, {
      titulo: 'Excluir sabor',
      mensagem: `Deseja excluir o sabor "${sabor.nome}"?`,
    }).subscribe((confirmou) => {
      if (!confirmou) return;

      this.saborService.excluir(sabor.id).subscribe({
        next: () => {
          this.notificacao.sucesso('Sabor excluído.');
          if (this.editandoId() === sabor.id) this.limparFormulario();
          if (this.sabores().length === 1 && this.pageIndex() > 0) this.pageIndex.update((p) => p - 1);
          this.carregar();
          this.carregarMetricasCatalogo();
        },
        error: (erro: HttpErrorResponse) =>
          this.notificacao.erro(mensagemDaApi(erro, 'Não foi possível excluir o sabor.')),
      });
    });
  }
}
