import { BreakpointObserver } from '@angular/cdk/layout';
import { Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { map } from 'rxjs';

interface ItemMenu {
  rota: string;
  rotulo: string;
  icone: string;
}

@Component({
  selector: 'app-admin-layout',
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatSidenavModule,
    MatToolbarModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
  ],
  templateUrl: './admin-layout.html',
  styleUrl: './admin-layout.css',
})
export class AdminLayout {
  private breakpoint = inject(BreakpointObserver);

  telaPequena = toSignal(this.breakpoint.observe('(max-width: 1023px)').pipe(map((r) => r.matches)), {
    initialValue: false,
  });

  menuMobileAberto = signal(false);

  menuCadastros: ItemMenu[] = [
    { rota: '/admin/produtos', rotulo: 'Produtos', icone: 'inventory_2' },
    { rota: '/admin/estoque', rotulo: 'Estoque & Lotes', icone: 'inventory' },
    { rota: '/admin/categorias', rotulo: 'Categorias', icone: 'category' },
    { rota: '/admin/marcas', rotulo: 'Marcas', icone: 'verified' },
    { rota: '/admin/sabores', rotulo: 'Sabores', icone: 'icecream' },
    { rota: '/admin/fornecedores', rotulo: 'Fornecedores', icone: 'local_shipping' },
    { rota: '/admin/clientes', rotulo: 'Clientes', icone: 'group' },
  ];

  fecharMenuMobile(): void {
    this.menuMobileAberto.set(false);
  }
}
