import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { MatBadgeModule } from '@angular/material/badge';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { CarrinhoService } from '../../../services/carrinho.service';
import { SessaoService } from '../../../services/sessao.service';

@Component({
  selector: 'app-loja-layout',
  imports: [RouterOutlet, RouterLink, MatButtonModule, MatIconModule, MatBadgeModule, MatMenuModule],
  templateUrl: './loja-layout.html',
  styleUrl: './loja-layout.css',
})
export class LojaLayout {
  private router = inject(Router);
  carrinho = inject(CarrinhoService);
  sessao = inject(SessaoService);

  ano = new Date().getFullYear();

  pesquisar(termo: string): void {
    this.router.navigate(['/loja'], { queryParams: { busca: termo.trim() || null } });
  }

  sair(): void {
    this.sessao.sair();
    this.router.navigateByUrl('/loja');
  }
}
