import { CurrencyPipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Fornecedor } from '../../../../models/fornecedor.model';
import { Produto } from '../../../../models/produto.model';
import { FornecedorService } from '../../../../services/fornecedor.service';
import { ProdutoService } from '../../../../services/produto.service';
import { FormatoPipe } from '../../../../shared/formato.pipe';
import { NotificacaoService } from '../../../../shared/notificacao.service';

@Component({
  selector: 'app-fornecedor-detalhe',
  imports: [RouterLink, CurrencyPipe, FormatoPipe],
  templateUrl: './fornecedor-detalhe.html',
})
export class FornecedorDetalhe implements OnInit {
  private route = inject(ActivatedRoute);
  private fornecedorService = inject(FornecedorService);
  private produtoService = inject(ProdutoService);
  private notificacao = inject(NotificacaoService);

  fornecedor = signal<Fornecedor | null>(null);
  produtos = signal<Produto[]>([]);
  filtroProduto = signal('');
  carregando = signal(false);
  erro = signal('');

  produtosFornecedor = computed(() => {
    const f = this.fornecedor();
    if (!f) return [];
    const termo = this.filtroProduto().trim().toLowerCase();
    return this.produtos()
      .filter((p) => p.fornecedor?.id === f.id)
      .filter(
        (p) =>
          !termo ||
          p.nome.toLowerCase().includes(termo) ||
          p.marca.nome.toLowerCase().includes(termo) ||
          p.categoria.nome.toLowerCase().includes(termo),
      );
  });

  estoqueTotalUnidades = computed(() =>
    this.produtosFornecedor().reduce((acc, p) => acc + (p.estoque || 0), 0),
  );

  valorTotalEstoque = computed(() =>
    this.produtosFornecedor().reduce((acc, p) => acc + p.preco * (p.estoque || 0), 0),
  );

  ngOnInit(): void {
    const idParam = Number(this.route.snapshot.paramMap.get('id'));
    if (!idParam) {
      this.erro.set('Fornecedor inválido.');
      return;
    }

    this.carregando.set(true);
    this.fornecedorService.buscarPorId(idParam).subscribe({
      next: (f) => {
        this.fornecedor.set(f);
        this.carregando.set(false);
      },
      error: () => {
        this.erro.set('Fornecedor não encontrado.');
        this.carregando.set(false);
      },
    });

    this.produtoService.listar({}, 0, 100).subscribe({
      next: (pag) => this.produtos.set(pag.items),
      error: () => {},
    });
  }

  iniciaisFornecedor(razaoSocial?: string): string {
    const partes = (razaoSocial || 'SB').trim().split(/\s+/);
    if (partes.length >= 2) {
      return (partes[0][0] + partes[1][0]).toUpperCase();
    }
    return (razaoSocial || 'SB').slice(0, 2).toUpperCase();
  }

  copiarTexto(valor: string, rotulo: string): void {
    if (typeof navigator !== 'undefined' && navigator.clipboard) {
      navigator.clipboard.writeText(valor);
    }
    this.notificacao.sucesso(`${rotulo} copiado para a área de transferência!`);
  }

  exportarFichaPdf(): void {
    this.notificacao.sucesso('Ficha cadastral do fornecedor gerada para impressão/PDF.');
    if (typeof window !== 'undefined') {
      window.print();
    }
  }
}
