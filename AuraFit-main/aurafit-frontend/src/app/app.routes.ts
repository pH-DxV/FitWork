import { Routes } from '@angular/router';
import { AdminLayout } from './components/admin/admin-layout/admin-layout';
import { CategoriaForm } from './components/admin/categorias/categoria-form/categoria-form';
import { CategoriaList } from './components/admin/categorias/categoria-list/categoria-list';
import { ClienteForm } from './components/admin/clientes/cliente-form/cliente-form';
import { ClienteList } from './components/admin/clientes/cliente-list/cliente-list';
import { FornecedorForm } from './components/admin/fornecedores/fornecedor-form/fornecedor-form';
import { FornecedorList } from './components/admin/fornecedores/fornecedor-list/fornecedor-list';
import { MarcaForm } from './components/admin/marcas/marca-form/marca-form';
import { MarcaList } from './components/admin/marcas/marca-list/marca-list';
import { Painel } from './components/admin/painel/painel';
import { ProdutoForm } from './components/admin/produtos/produto-form/produto-form';
import { ProdutoList } from './components/admin/produtos/produto-list/produto-list';
import { SaborForm } from './components/admin/sabores/sabor-form/sabor-form';
import { SaborList } from './components/admin/sabores/sabor-list/sabor-list';
import { Carrinho } from './components/loja/carrinho/carrinho';
import { Catalogo } from './components/loja/catalogo/catalogo';
import { LojaLayout } from './components/loja/loja-layout/loja-layout';
import { Login } from './components/loja/login/login';
import { Pagamento } from './components/loja/pagamento/pagamento';
import { Perfil } from './components/loja/perfil/perfil';

export const routes: Routes = [
  { path: '', redirectTo: 'loja', pathMatch: 'full' },

  // ---------- e-commerce (protótipo) ----------
  {
    path: 'loja',
    component: LojaLayout,
    children: [
      { path: '', component: Catalogo, title: 'AuraFit | Suplementos' },
      { path: 'carrinho', component: Carrinho, title: 'AuraFit | Carrinho' },
      { path: 'pagamento', component: Pagamento, title: 'AuraFit | Pagamento' },
      { path: 'login', component: Login, title: 'AuraFit | Entrar' },
      { path: 'perfil', component: Perfil, title: 'AuraFit | Minha conta' },
    ],
  },

  // ---------- área administrativa ----------
  {
    path: 'admin',
    component: AdminLayout,
    children: [
      { path: '', component: Painel, title: 'Admin | Início' },

      { path: 'categorias', component: CategoriaList, title: 'Admin | Categorias' },
      { path: 'categorias/nova', component: CategoriaForm, title: 'Admin | Nova categoria' },
      { path: 'categorias/editar/:id', component: CategoriaForm, title: 'Admin | Editar categoria' },

      { path: 'marcas', component: MarcaList, title: 'Admin | Marcas' },
      { path: 'marcas/nova', component: MarcaForm, title: 'Admin | Nova marca' },
      { path: 'marcas/editar/:id', component: MarcaForm, title: 'Admin | Editar marca' },

      { path: 'sabores', component: SaborList, title: 'Admin | Sabores' },
      { path: 'sabores/novo', component: SaborForm, title: 'Admin | Novo sabor' },
      { path: 'sabores/editar/:id', component: SaborForm, title: 'Admin | Editar sabor' },

      { path: 'fornecedores', component: FornecedorList, title: 'Admin | Fornecedores' },
      { path: 'fornecedores/novo', component: FornecedorForm, title: 'Admin | Novo fornecedor' },
      { path: 'fornecedores/editar/:id', component: FornecedorForm, title: 'Admin | Editar fornecedor' },

      { path: 'produtos', component: ProdutoList, title: 'Admin | Produtos' },
      { path: 'produtos/novo', component: ProdutoForm, title: 'Admin | Novo produto' },
      { path: 'produtos/editar/:id', component: ProdutoForm, title: 'Admin | Editar produto' },

      { path: 'clientes', component: ClienteList, title: 'Admin | Clientes' },
      { path: 'clientes/novo', component: ClienteForm, title: 'Admin | Novo cliente' },
      { path: 'clientes/editar/:id', component: ClienteForm, title: 'Admin | Editar cliente' },
    ],
  },

  { path: '**', redirectTo: 'loja' },
];
