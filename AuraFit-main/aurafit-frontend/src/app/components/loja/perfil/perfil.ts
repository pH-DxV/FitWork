import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { SessaoService } from '../../../services/sessao.service';
import { NotificacaoService } from '../../../shared/notificacao.service';

/** Protótipo da tela de perfil do usuário da loja. */
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

  iniciais(nome: string): string {
    return nome
      .split(' ')
      .filter((parte) => parte)
      .slice(0, 2)
      .map((parte) => parte[0].toUpperCase())
      .join('');
  }

  funcionalidadePrototipo(acao: string): void {
    this.notificacao.sucesso(`${acao} estará disponível após integração com JWT.`);
  }

  sair(): void {
    this.sessao.sair();
    this.router.navigateByUrl('/loja');
  }
}
