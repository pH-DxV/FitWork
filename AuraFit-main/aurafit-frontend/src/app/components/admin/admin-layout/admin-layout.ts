import { BreakpointObserver } from '@angular/cdk/layout';
import { Component, inject } from '@angular/core';
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
    RouterOutlet, RouterLink, RouterLinkActive, MatSidenavModule, MatToolbarModule,
    MatListModule, MatIconModule, MatButtonModule,
  ],
  templateUrl: './admin-layout.html',
  styleUrl: './admin-layout.css',
})
export class AdminLayout {
  private breakpoint = inject(BreakpointObserver);

  /** Em telas pequenas o menu vira "gaveta" (abre pelo botão). */
  telaPequena = toSignal(this.breakpoint.observe('(max-width: 900px)').pipe(map((r) => r.matches)), {
    initialValue: false,
  });

  menuCadastros: ItemMenu[] = [
    { rota: '/admin/produtos', rotulo: 'Produtos', icone: 'inventory_2' },
    { rota: '/admin/categorias', rotulo: 'Categorias', icone: 'category' },
    { rota: '/admin/marcas', rotulo: 'Marcas', icone: 'verified' },
    { rota: '/admin/sabores', rotulo: 'Sabores', icone: 'icecream' },
    { rota: '/admin/fornecedores', rotulo: 'Fornecedores', icone: 'local_shipping' },
    { rota: '/admin/clientes', rotulo: 'Clientes', icone: 'groups' },
  ];
}
