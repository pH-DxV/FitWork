import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api.config';
import { Fornecedor, FornecedorRequest } from '../models/fornecedor.model';
import { Pagina } from '../models/pagina.model';
import { paginacao, carregarTodasPaginas } from './http-params';

@Injectable({ providedIn: 'root' })
export class FornecedorService {
  private http = inject(HttpClient);
  private readonly url = `${API_URL}/fornecedores`;

  /** Lista paginada com filtro opcional via query parameter (?nome=). */
  listar(filtro = '', page = 0, pageSize = 10): Observable<Pagina<Fornecedor>> {
    let params = paginacao(page, pageSize);
    const termo = filtro.trim();
    if (termo) {
      params = params.set('nome', termo);
    }
    return this.http.get<Pagina<Fornecedor>>(this.url, { params });
  }

  listarTodos(): Observable<Fornecedor[]> {
    return carregarTodasPaginas((page, pageSize) => this.listar('', page, pageSize));
  }

  buscarPorId(id: number): Observable<Fornecedor> {
    return this.http.get<Fornecedor>(`${this.url}/${id}`);
  }

  incluir(dados: FornecedorRequest): Observable<Fornecedor> {
    return this.http.post<Fornecedor>(this.url, dados);
  }

  alterar(id: number, dados: FornecedorRequest): Observable<Fornecedor> {
    return this.http.put<Fornecedor>(`${this.url}/${id}`, dados);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
