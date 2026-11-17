import { CurrencyPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatBadgeModule } from '@angular/material/badge';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { CarrinhoService } from '../../../services/carrinho.service';
import { SessaoService } from '../../../services/sessao.service';

@Component({
  selector: 'app-loja-layout',
  imports: [
    CurrencyPipe,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatButtonModule,
    MatIconModule,
    MatBadgeModule,
    MatMenuModule,
  ],
  templateUrl: './loja-layout.html',
  styleUrl: './loja-layout.css',
})
export class LojaLayout {
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  carrinho = inject(CarrinhoService);
  sessao = inject(SessaoService);

  termoBusca = signal('');
  ano = new Date().getFullYear();

  constructor() {
    this.route.queryParamMap.pipe(takeUntilDestroyed()).subscribe((params) => {
      this.termoBusca.set(params.get('busca') ?? '');
    });
  }

  pesquisar(termo: string): void {
    this.router.navigate(['/loja'], { queryParams: { busca: termo.trim() || null } });
  }

  limparBusca(input: HTMLInputElement): void {
    input.value = '';
    this.termoBusca.set('');
    this.pesquisar('');
  }

  sair(): void {
    this.sessao.sair();
    this.router.navigateByUrl('/loja');
  }
}
