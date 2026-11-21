import { Categoria } from './categoria.model';
import { Fornecedor } from './fornecedor.model';
import { Marca } from './marca.model';
import { Sabor } from './sabor.model';
import { TipoProteina } from './tipo-proteina.model';

export interface Produto {
  id: number;
  nome: string;
  descricao: string | null;
  preco: number;
  pesoGramas: number;
  estoque: number;
  tipoProteina: TipoProteina;
  ativo: boolean;
  categoria: Categoria;
  marca: Marca;
  fornecedor: Fornecedor;
  sabores: Sabor[];
  nomeImagem?: string | null;
  imagemUrl?: string | null;
}

export interface ProdutoRequest {
  nome: string;
  descricao: string;
  preco: number;
  pesoGramas: number;
  estoque: number;
  idTipoProteina: number;
  ativo: boolean;
  idCategoria: number;
  idMarca: number;
  idFornecedor: number;
  idsSabores: number[];
}

export interface FiltroProduto {
  nome?: string;
  idTipoProteina?: number | null;
  idCategoria?: number | null;
  idMarca?: number | null;
  ativo?: boolean | null;
}
