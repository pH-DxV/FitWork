import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { SessaoService } from '../../../services/sessao.service';
import { NotificacaoService } from '../../../shared/notificacao.service';

/** Tela de Meus Pedidos B2B, Faturamento & Perfil do Lojista (Google Stitch 04). */
@Component({
  selector: 'app-perfil',
  imports: [CurrencyPipe, DatePipe, RouterLink, MatButtonModule, MatIconModule, MatTabsModule],
  templateUrl: './perfil.html',
  styleUrl: './perfil.css',
})
export class Perfil {
  sessao = inject(SessaoService);
  private notificacao = inject(NotificacaoService);
  private router = inject(Router);

  buscaPedido = signal('');
  filtroSituacao = signal('todos');
  abaAtiva = signal<'pedidos' | 'cadastro'>('pedidos');

  readonly linhaDigitavelAberta = '34191.79001 01043.510047 91020.150008 8 98950000182900';

  totalFaturado = computed(() =>
    this.sessao.pedidos().reduce((soma, p) => soma + p.total, 0),
  );

  totalEmAberto = computed(() =>
    this.sessao
      .pedidos()
      .filter((p) => p.situacao === 'Aguardando pagamento')
      .reduce((soma, p) => soma + p.total, 0),
  );

  pedidosEmTransito = computed(() =>
    this.sessao.pedidos().filter((p) => p.situacao === 'Em trânsito').length,
  );

  pedidosFiltrados = computed(() => {
    const termo = this.buscaPedido().trim().toLowerCase();
    const situacao = this.filtroSituacao();
    return this.sessao.pedidos().filter((p) => {
      const bateTermo =
        !termo ||
        p.numero.toLowerCase().includes(termo) ||
        p.formaPagamento.toLowerCase().includes(termo);
      const bateSituacao = situacao === 'todos' || p.situacao === situacao;
      return bateTermo && bateSituacao;
    });
  });

  iniciais(nome: string): string {
    return nome
      .split(' ')
      .filter((parte) => parte)
      .slice(0, 2)
      .map((parte) => parte[0].toUpperCase())
      .join('');
  }

  copiarCodigoBarras(): void {
    if (typeof navigator !== 'undefined' && navigator.clipboard) {
      navigator.clipboard.writeText(this.linhaDigitavelAberta);
    }
    this.notificacao.sucesso('Linha digitável do boleto copiada para a área de transferência.');
  }

  baixarDocumento(tipo: string, numero: string): void {
    this.notificacao.sucesso(`Iniciando download de ${tipo} do pedido #${numero}...`);
  }

  funcionalidadePrototipo(acao: string): void {
    this.notificacao.sucesso(`${acao} processado no portal B2B.`);
  }

  sair(): void {
    this.sessao.sair();
    this.router.navigateByUrl('/loja');
  }
}
