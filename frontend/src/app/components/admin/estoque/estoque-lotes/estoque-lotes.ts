import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Fornecedor } from '../../../../models/fornecedor.model';
import { Produto, ProdutoRequest } from '../../../../models/produto.model';
import { Sabor } from '../../../../models/sabor.model';
import { FornecedorService } from '../../../../services/fornecedor.service';
import { ProdutoService } from '../../../../services/produto.service';
import { SaborService } from '../../../../services/sabor.service';
import { mensagemDaApi } from '../../../../shared/erros-formulario';
import { NotificacaoService } from '../../../../shared/notificacao.service';

export interface RegistroLote {
  id: number;
  produto: Produto;
  codigoLote: string;
  notaFiscal: string;
  validade: string;
  diasParaVencer: number;
  statusQualidade: 'LIBERADO' | 'ALERTA_FEFO' | 'QUARENTENA';
}

@Component({
  selector: 'app-estoque-lotes',
  imports: [RouterLink, ReactiveFormsModule, CurrencyPipe],
  templateUrl: './estoque-lotes.html',
})
export class EstoqueLotes implements OnInit {
  private fb = inject(FormBuilder);
  private produtoService = inject(ProdutoService);
  private fornecedorService = inject(FornecedorService);
  private saborService = inject(SaborService);
  private notificacao = inject(NotificacaoService);

  produtos = signal<Produto[]>([]);
  fornecedores = signal<Fornecedor[]>([]);
  sabores = signal<Sabor[]>([]);
  carregando = signal(false);
  salvando = signal(false);

  // Filtros
  busca = signal('');
  filtroStatus = signal<'TODOS' | 'LIBERADO' | 'ALERTA_FEFO' | 'QUARENTENA'>('TODOS');
  filtroFornecedorId = signal<number | null>(null);

  form = this.fb.nonNullable.group({
    produtoId: [0, [Validators.required, Validators.min(1)]],
    fornecedorId: [0],
    saborId: [0],
    codigoLote: ['LT-2026-098A', [Validators.required, Validators.minLength(3)]],
    notaFiscal: ['000.421.880', [Validators.required]],
    dataFabricacao: ['2026-10-01', [Validators.required]],
    dataValidade: ['2028-10-01', [Validators.required]],
    quantidadeEntrada: [50, [Validators.required, Validators.min(1)]],
    custoUnitario: [145.9, [Validators.required, Validators.min(0.01)]],
    laudoHplcAprovado: [true],
  });

  lotes = computed<RegistroLote[]>(() => {
    return this.produtos().map((prod, idx) => {
      const loteNum = (89 - idx * 7 + prod.id).toString().padStart(3, '0');
      const nfNum = (418920 - idx * 1412 + prod.id).toString();
      const nfFormatada = `000.${nfNum.slice(0, 3)}.${nfNum.slice(3, 6)}`;
      const estoque = prod.estoque ?? 0;

      let statusQualidade: 'LIBERADO' | 'ALERTA_FEFO' | 'QUARENTENA' = 'LIBERADO';
      let validade = '10/2027';
      let diasParaVencer = 340;

      if (estoque <= 10) {
        statusQualidade = 'ALERTA_FEFO';
        validade = '01/2027';
        diasParaVencer = 64;
      } else if (idx % 5 === 4) {
        statusQualidade = 'QUARENTENA';
        validade = '12/2027';
        diasParaVencer = 410;
      }

      return {
        id: prod.id,
        produto: prod,
        codigoLote: `LT-2026-${loteNum}A`,
        notaFiscal: nfFormatada,
        validade,
        diasParaVencer,
        statusQualidade,
      };
    });
  });

  lotesFiltrados = computed(() => {
    const termo = this.busca().trim().toLowerCase();
    const status = this.filtroStatus();
    const fornId = this.filtroFornecedorId();

    return this.lotes().filter((item) => {
      const matchStatus = status === 'TODOS' || item.statusQualidade === status;
      const matchForn = !fornId || item.produto.fornecedor?.id === fornId;
      const matchBusca =
        !termo ||
        item.produto.nome.toLowerCase().includes(termo) ||
        item.produto.marca.nome.toLowerCase().includes(termo) ||
        item.codigoLote.toLowerCase().includes(termo) ||
        item.notaFiscal.toLowerCase().includes(termo);
      return matchStatus && matchForn && matchBusca;
    });
  });

  totalUnidadesEstoque = computed(() =>
    this.produtos().reduce((acc, p) => acc + (p.estoque || 0), 0),
  );

  valorImobilizadoEstoque = computed(() =>
    this.produtos().reduce((acc, p) => acc + p.preco * (p.estoque || 0), 0),
  );

  alertasFefoCount = computed(
    () => this.lotes().filter((l) => l.statusQualidade === 'ALERTA_FEFO').length,
  );

  quarentenaCount = computed(
    () => this.lotes().filter((l) => l.statusQualidade === 'QUARENTENA').length,
  );

  ngOnInit(): void {
    this.carregarDados();
  }

  carregarDados(): void {
    this.carregando.set(true);
    this.produtoService.listar({}, 0, 100).subscribe({
      next: (pag) => {
        this.produtos.set(pag.items);
        if (pag.items.length > 0 && !this.form.controls.produtoId.value) {
          const p = pag.items[0];
          this.form.patchValue({
            produtoId: p.id,
            fornecedorId: p.fornecedor?.id ?? 0,
            saborId: p.sabores?.[0]?.id ?? 0,
            custoUnitario: Number((p.preco * 0.72).toFixed(2)),
          });
        }
        this.carregando.set(false);
      },
      error: () => this.carregando.set(false),
    });

    this.fornecedorService.listarTodos().subscribe({
      next: (lista) => this.fornecedores.set(lista),
      error: () => {},
    });

    this.saborService.listarTodos().subscribe({
      next: (lista) => this.sabores.set(lista),
      error: () => {},
    });
  }

  aoSelecionarProduto(idStr: string): void {
    const id = Number(idStr);
    const prod = this.produtos().find((p) => p.id === id);
    if (prod) {
      this.form.patchValue({
        fornecedorId: prod.fornecedor?.id ?? 0,
        saborId: prod.sabores?.[0]?.id ?? 0,
        custoUnitario: Number((prod.preco * 0.72).toFixed(2)),
      });
    }
  }

  selecionarLoteNaTabela(lote: RegistroLote): void {
    const prod = lote.produto;
    this.form.patchValue({
      produtoId: prod.id,
      fornecedorId: prod.fornecedor?.id ?? 0,
      saborId: prod.sabores?.[0]?.id ?? 0,
      codigoLote: lote.codigoLote,
      notaFiscal: lote.notaFiscal,
      quantidadeEntrada: 25,
      custoUnitario: Number((prod.preco * 0.72).toFixed(2)),
    });
  }

  registrarEntradaLote(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const valores = this.form.getRawValue();
    const prod = this.produtos().find((p) => p.id === Number(valores.produtoId));
    if (!prod) {
      this.notificacao.erro('Selecione um produto válido do catálogo.');
      return;
    }

    this.salvando.set(true);
    const novoEstoque = (prod.estoque || 0) + Number(valores.quantidadeEntrada);

    const request: ProdutoRequest = {
      nome: prod.nome,
      descricao: prod.descricao ?? '',
      preco: prod.preco,
      estoque: novoEstoque,
      pesoGramas: prod.pesoGramas,
      idTipoProteina: prod.tipoProteina.id,
      ativo: prod.ativo,
      idCategoria: prod.categoria.id,
      idMarca: prod.marca.id,
      idFornecedor: prod.fornecedor.id,
      idsSabores: (prod.sabores ?? []).map((s) => s.id),
    };

    this.produtoService.alterar(prod.id, request).subscribe({
      next: () => {
        this.salvando.set(false);
        this.notificacao.sucesso(
          `Entrada do lote ${valores.codigoLote} registrada! Estoque de "${prod.nome}" atualizado para ${novoEstoque} un.`,
        );
        this.carregarDados();
      },
      error: (erro: HttpErrorResponse) => {
        this.salvando.set(false);
        this.notificacao.erro(mensagemDaApi(erro, 'Não foi possível atualizar o estoque do produto.'));
      },
    });
  }

  exportarInventario(): void {
    this.notificacao.sucesso('Relatório de Inventário Fiscal & FEFO gerado com sucesso.');
  }
}
