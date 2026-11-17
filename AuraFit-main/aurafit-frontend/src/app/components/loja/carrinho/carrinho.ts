import { CurrencyPipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { CarrinhoService, FRETE_GRATIS_A_PARTIR, ItemCarrinho } from '../../../services/carrinho.service';
import { SessaoService } from '../../../services/sessao.service';

@Component({
  selector: 'app-carrinho',
  imports: [CurrencyPipe, RouterLink, MatButtonModule, MatIconModule],
  templateUrl: './carrinho.html',
  styleUrl: './carrinho.css',
})
export class Carrinho {
  carrinho = inject(CarrinhoService);
  sessao = inject(SessaoService);
  private router = inject(Router);

  readonly freteGratisAPartir = FRETE_GRATIS_A_PARTIR;

  faltaParaFreteGratis(): number {
    return Math.max(FRETE_GRATIS_A_PARTIR - this.carrinho.subtotal(), 0);
  }

  progressoFrete(): number {
    return Math.min((this.carrinho.subtotal() / FRETE_GRATIS_A_PARTIR) * 100, 100);
  }

  pesoTotalKg(): string {
    const gramas = this.carrinho
      .itens()
      .reduce((soma, item) => soma + item.produto.pesoGramas * item.quantidade, 0);
    return (gramas / 1000).toFixed(2).replace('.', ',');
  }

  alterar(item: ItemCarrinho, delta: number): void {
    this.carrinho.alterarQuantidade(item, item.quantidade + delta);
  }

  finalizar(): void {
    if (!this.sessao.usuario()) {
      this.router.navigate(['/loja/login'], { queryParams: { retorno: '/loja/pagamento' } });
      return;
    }
    this.router.navigateByUrl('/loja/pagamento');
  }
}
