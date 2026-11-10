import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api.config';
import { Fornecedor, FornecedorRequest } from '../models/fornecedor.model';
import { Pagina } from '../models/pagina.model';
import { paginacao } from './http-params';

@Injectable({ providedIn: 'root' })
export class FornecedorService {
  private http = inject(HttpClient);
  private readonly url = `${API_URL}/fornecedores`;

  /** Lista paginada; quando o filtro é informado usa o endpoint /fornecedores/nome/{nome}. */
  listar(filtro = '', page = 0, pageSize = 10): Observable<Pagina<Fornecedor>> {
    const params = paginacao(page, pageSize);
    const termo = filtro.trim();
    const url = termo ? `${this.url}/nome/${encodeURIComponent(termo)}` : this.url;
    return this.http.get<Pagina<Fornecedor>>(url, { params });
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
