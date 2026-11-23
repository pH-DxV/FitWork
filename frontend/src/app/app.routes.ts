import { Routes } from '@angular/router';
import { AdminLayout } from './components/admin/admin-layout/admin-layout';
import { CategoriaForm } from './components/admin/categorias/categoria-form/categoria-form';
import { CategoriaList } from './components/admin/categorias/categoria-list/categoria-list';
import { ClienteForm } from './components/admin/clientes/cliente-form/cliente-form';
import { ClienteList } from './components/admin/clientes/cliente-list/cliente-list';
import { EstoqueLotes } from './components/admin/estoque/estoque-lotes/estoque-lotes';
import { FornecedorDetalhe } from './components/admin/fornecedores/fornecedor-detalhe/fornecedor-detalhe';
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
import { adminGuard, authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'loja', pathMatch: 'full' },

  // ---------- e-commerce (vitrine pública & áreas autenticadas do cliente) ----------
  {
    path: 'loja',
    component: LojaLayout,
    children: [
      { path: '', component: Catalogo, title: 'FitWork | Vitrine & Loja Oficial' },
      { path: 'carrinho', component: Carrinho, title: 'FitWork | Cotação B2B & Carrinho' },
      { path: 'login', component: Login, title: 'FitWork | Acesso Corporativo & Lojista' },
      {
        path: 'pagamento',
        component: Pagamento,
        canActivate: [authGuard],
        title: 'FitWork | Checkout & Faturamento B2B',
      },
      {
        path: 'perfil',
        component: Perfil,
        canActivate: [authGuard],
        title: 'FitWork | Portal do Cliente & Meus Pedidos',
      },
    ],
  },

  // ---------- área administrativa (restrita ao papel ADMIN) ----------
  {
    path: 'admin',
    component: AdminLayout,
    canActivate: [adminGuard],
    canActivateChild: [adminGuard],
    children: [
      { path: '', component: Painel, title: 'Admin | Painel Geral' },
      { path: 'painel', redirectTo: '', pathMatch: 'full' },

      { path: 'categorias', component: CategoriaList, title: 'Admin | Gestão de Categorias' },
      { path: 'categorias/nova', component: CategoriaForm, title: 'Admin | Nova Categoria' },
      { path: 'categorias/editar/:id', component: CategoriaForm, title: 'Admin | Editar Categoria' },

      { path: 'marcas', component: MarcaList, title: 'Admin | Gestão de Marcas' },
      { path: 'marcas/nova', component: MarcaForm, title: 'Admin | Nova Marca' },
      { path: 'marcas/editar/:id', component: MarcaForm, title: 'Admin | Editar Marca' },

      { path: 'sabores', component: SaborList, title: 'Admin | Gestão de Sabores' },
      { path: 'sabores/novo', component: SaborForm, title: 'Admin | Novo Sabor' },
      { path: 'sabores/editar/:id', component: SaborForm, title: 'Admin | Editar Sabor' },

      { path: 'fornecedores', component: FornecedorList, title: 'Admin | Gestão de Fornecedores' },
      { path: 'fornecedores/novo', component: FornecedorForm, title: 'Admin | Novo Fornecedor' },
      { path: 'fornecedores/editar/:id', component: FornecedorForm, title: 'Admin | Editar Fornecedor' },
      { path: 'fornecedores/detalhes/:id', component: FornecedorDetalhe, title: 'Admin | Detalhes do Fornecedor' },

      { path: 'produtos', component: ProdutoList, title: 'Admin | Gestão de Suplementos' },
      { path: 'produtos/novo', component: ProdutoForm, title: 'Admin | Novo Suplemento' },
      { path: 'produtos/editar/:id', component: ProdutoForm, title: 'Admin | Editar Suplemento' },

      { path: 'estoque', component: EstoqueLotes, title: 'Admin | Gestão de Estoque e Lotes' },

      { path: 'clientes', component: ClienteList, title: 'Admin | Base de Clientes' },
      { path: 'clientes/novo', component: ClienteForm, title: 'Admin | Novo Cliente' },
      { path: 'clientes/editar/:id', component: ClienteForm, title: 'Admin | Editar Cliente' },
    ],
  },

  { path: '**', redirectTo: 'loja' },
];
