import { Municipio } from './estado.model';

export interface Telefone {
  codigoArea: string;
  numero: string;
  whatsapp: boolean;
}

export interface Endereco {
  cep: string;
  logradouro: string;
  numero: string;
  complemento: string | null;
  bairro: string;
  municipio: Municipio;
}

export interface Cliente {
  id: number;
  nome: string;
  email: string;
  cpf: string;
  dataNascimento: string;
  genero: string | null;
  telefones: Telefone[];
  endereco: Endereco;
  aceitaNovidades: boolean;
  ativo: boolean;
  dataCadastro: string;
}

export interface EnderecoRequest {
  cep: string;
  logradouro: string;
  numero: string;
  complemento: string;
  bairro: string;
  cidade: string;
  uf: string;
  codigoIbge: string | null;
}

export interface ClienteRequest {
  nome: string;
  email: string;
  cpf: string;
  dataNascimento: string;
  genero: string;
  telefones: Telefone[];
  endereco: EnderecoRequest;
  aceitaNovidades: boolean;
  ativo: boolean;
}
