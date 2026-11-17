export interface Marca {
  id: number;
  nome: string;
  cnpj: string;
  emailContato: string | null;
  ativa: boolean;
}

export interface MarcaRequest {
  nome: string;
  cnpj: string;
  emailContato: string;
  ativa: boolean;
}
