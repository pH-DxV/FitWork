export interface Fornecedor {
  id: number;
  razaoSocial: string;
  cnpj: string;
  telefone: string;
  email: string | null;
}

export interface FornecedorRequest {
  razaoSocial: string;
  cnpj: string;
  telefone: string;
  email: string;
}
