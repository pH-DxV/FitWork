export interface Regiao {
  id: number;
  nome: string;
}

export interface Estado {
  id: number;
  nome: string;
  sigla: string;
  regiao: Regiao;
}

export interface Municipio {
  id: number;
  nome: string;
  codigoIbge: string | null;
  estado: Estado;
}
