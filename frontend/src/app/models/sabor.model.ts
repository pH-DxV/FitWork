export interface Sabor {
  id: number;
  nome: string;
  descricao: string | null;
}

export interface SaborRequest {
  nome: string;
  descricao: string;
}
