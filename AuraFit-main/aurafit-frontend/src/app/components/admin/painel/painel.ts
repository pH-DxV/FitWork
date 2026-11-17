import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { forkJoin } from 'rxjs';
import { Cliente } from '../../../models/cliente.model';
import { Produto } from '../../../models/produto.model';
import { CategoriaService } from '../../../services/categoria.service';
import { ClienteService } from '../../../services/cliente.service';
import { FornecedorService } from '../../../services/fornecedor.service';
import { MarcaService } from '../../../services/marca.service';
import { ProdutoService } from '../../../services/produto.service';
import { SaborService } from '../../../services/sabor.service';

interface Indicador {
  rotulo: string;
  subtitulo: string;
  valor: number;
  icone: string;
  rota: string;
  destaque: string;
}

interface DistribuicaoItem {
  nome: string;
  quantidade: number;
  percentual: number;
}

interface AtividadeRecente {
  codigo: string;
  entidade: string;
  modulo: string;
  detalhe: string;
  status: string;
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
  private saborService = inject(SaborService);
  private fornecedorService = inject(FornecedorService);

  indicadores = signal<Indicador[]>([]);
  produtosAmostra = signal<Produto[]>([]);
  clientesAmostra = signal<Cliente[]>([]);
  erro = signal('');
  carregando = signal(false);

  distribuicaoProteina = computed<DistribuicaoItem[]>(() => {
    const produtos = this.produtosAmostra();
    const total = produtos.length || 1;
    const mapa = new Map<string, number>();
    for (const p of produtos) {
      const chave = p.tipoProteina?.nome ?? 'Outros';
      mapa.set(chave, (mapa.get(chave) ?? 0) + 1);
    }
    if (mapa.size === 0) {
      return [
        { nome: 'Isolado (WPI)', quantidade: 0, percentual: 0 },
        { nome: 'Concentrado (WPC)', quantidade: 0, percentual: 0 },
        { nome: 'Hidrolisado (WPH)', quantidade: 0, percentual: 0 },
        { nome: 'Vegano / Plant-Based', quantidade: 0, percentual: 0 },
      ];
    }
    return Array.from(mapa.entries())
      .map(([nome, quantidade]) => ({
        nome,
        quantidade,
        percentual: +((quantidade / total) * 100).toFixed(1),
      }))
      .sort((a, b) => b.quantidade - a.quantidade);
  });

  distribuicaoRegiao = computed<DistribuicaoItem[]>(() => {
    const clientes = this.clientesAmostra();
    const total = clientes.length || 1;
    const mapa = new Map<string, number>();
    for (const c of clientes) {
      const regiao = c.endereco?.municipio?.estado?.regiao?.nome ?? 'Sudeste';
      mapa.set(regiao, (mapa.get(regiao) ?? 0) + 1);
    }
    if (mapa.size === 0) {
      return [
        { nome: 'Sudeste', quantidade: 0, percentual: 0 },
        { nome: 'Sul', quantidade: 0, percentual: 0 },
        { nome: 'Centro-Oeste / Norte / Nordeste', quantidade: 0, percentual: 0 },
      ];
    }
    return Array.from(mapa.entries())
      .map(([nome, quantidade]) => ({
        nome,
        quantidade,
        percentual: +((quantidade / total) * 100).toFixed(1),
      }))
      .sort((a, b) => b.quantidade - a.quantidade);
  });

  atividadesRecentes = computed<AtividadeRecente[]>(() => {
    const itens: AtividadeRecente[] = [];
    for (const p of this.produtosAmostra().slice(0, 3)) {
      itens.push({
        codigo: `#PRD-${String(p.id).padStart(3, '0')}`,
        entidade: p.nome,
        modulo: 'Produto (ManyToOne + ManyToMany)',
        detalhe: `${p.marca.nome} · Estoque: ${p.estoque} un`,
        status: p.ativo ? 'Ativo' : 'Inativo',
        rota: `/admin/produtos/editar/${p.id}`,
      });
    }
    for (const c of this.clientesAmostra().slice(0, 2)) {
      itens.push({
        codigo: `#CLI-${String(c.id).padStart(3, '0')}`,
        entidade: c.nome,
        modulo: 'Cliente (Pessoa + ViaCEP)',
        detalhe: `${c.endereco?.municipio?.nome ?? ''} / ${c.endereco?.municipio?.estado?.sigla ?? ''}`,
        status: c.ativo ? 'Ativo' : 'Inativo',
        rota: `/admin/clientes/editar/${c.id}`,
      });
    }
    return itens;
  });

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando.set(true);
    this.erro.set('');
    forkJoin({
      produtos: this.produtoService.listar({}, 0, 50),
      categorias: this.categoriaService.listar('', 0, 1),
      marcas: this.marcaService.listar('', 0, 1),
      sabores: this.saborService.listar('', 0, 1),
      fornecedores: this.fornecedorService.listar('', 0, 1),
      clientes: this.clienteService.listar('', 0, 50),
    }).subscribe({
      next: (r) => {
        this.produtosAmostra.set(r.produtos.items);
        this.clientesAmostra.set(r.clientes.items);
        this.indicadores.set([
          {
            rotulo: 'Produtos',
            subtitulo: 'Catálogo & SKUs',
            valor: r.produtos.totalItems,
            icone: 'inventory_2',
            rota: '/admin/produtos',
            destaque: 'Controle de estoque ativo',
          },
          {
            rotulo: 'Categorias',
            subtitulo: 'Taxonomia B2B',
            valor: r.categorias.totalItems,
            icone: 'category',
            rota: '/admin/categorias',
            destaque: 'Classificação nutricional',
          },
          {
            rotulo: 'Marcas',
            subtitulo: 'Fabricantes Homologados',
            valor: r.marcas.totalItems,
            icone: 'verified',
            rota: '/admin/marcas',
            destaque: 'CNPJ validado',
          },
          {
            rotulo: 'Sabores',
            subtitulo: 'Variações Sensoriais',
            valor: r.sabores.totalItems,
            icone: 'icecream',
            rota: '/admin/sabores',
            destaque: 'Relação N:N com Produtos',
          },
          {
            rotulo: 'Fornecedores',
            subtitulo: 'Suprimento Industrial',
            valor: r.fornecedores.totalItems,
            icone: 'local_shipping',
            rota: '/admin/fornecedores',
            destaque: 'Rastreabilidade de lotes',
          },
          {
            rotulo: 'Clientes',
            subtitulo: 'Base B2B & Varejo',
            valor: r.clientes.totalItems,
            icone: 'group',
            rota: '/admin/clientes',
            destaque: 'Integração ViaCEP / IBGE',
          },
        ]);
        this.carregando.set(false);
      },
      error: () => {
        this.erro.set('Não foi possível carregar os indicadores. O back-end está rodando?');
        this.carregando.set(false);
      },
    });
  }
}
