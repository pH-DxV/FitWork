import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { forkJoin } from 'rxjs';
import { CategoriaService } from '../../../services/categoria.service';
import { ClienteService } from '../../../services/cliente.service';
import { MarcaService } from '../../../services/marca.service';
import { ProdutoService } from '../../../services/produto.service';

interface Indicador {
  rotulo: string;
  valor: number;
  icone: string;
  rota: string;
}

@Component({
  selector: 'app-painel',
  imports: [RouterLink, MatIconModule, MatButtonModule],
  templateUrl: './painel.html',
  styleUrl: './painel.css',
})
export class Painel implements OnInit {
  private produtoService = inject(ProdutoService);
  private clienteService = inject(ClienteService);
  private marcaService = inject(MarcaService);
  private categoriaService = inject(CategoriaService);

  indicadores = signal<Indicador[]>([]);
  erro = signal('');

  ngOnInit(): void {
    forkJoin({
      produtos: this.produtoService.listar({}, 0, 1),
      clientes: this.clienteService.listar('', 0, 1),
      marcas: this.marcaService.listar('', 0, 1),
      categorias: this.categoriaService.listar('', 0, 1),
    }).subscribe({
      next: (r) =>
        this.indicadores.set([
          { rotulo: 'Produtos', valor: r.produtos.totalItems, icone: 'inventory_2', rota: '/admin/produtos' },
          { rotulo: 'Clientes', valor: r.clientes.totalItems, icone: 'groups', rota: '/admin/clientes' },
          { rotulo: 'Marcas', valor: r.marcas.totalItems, icone: 'verified', rota: '/admin/marcas' },
          { rotulo: 'Categorias', valor: r.categorias.totalItems, icone: 'category', rota: '/admin/categorias' },
        ]),
      error: () => this.erro.set('Não foi possível carregar os indicadores. O back-end está rodando?'),
    });
  }
}
