import { Cliente, EnderecoRequest, Telefone } from './cliente.model';

export type PerfilUsuario = 'ADMIN' | 'CLIENTE';

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface CadastroContaRequest {
  nome: string;
  email: string;
  senha: string;
  cpf: string;
  dataNascimento: string;
  genero: string;
  telefones: Telefone[];
  endereco: EnderecoRequest;
  aceitaNovidades: boolean;
}

export interface UsuarioAutenticado {
  keycloakId: string;
  nome: string;
  email: string;
  perfis: PerfilUsuario[];
  cliente: Cliente | null;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  usuario: UsuarioAutenticado;
}
