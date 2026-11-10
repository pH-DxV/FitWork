import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api.config';
import { Cliente, ClienteRequest } from '../models/cliente.model';
import { Pagina } from '../models/pagina.model';
import { paginacao } from './http-params';

@Injectable({ providedIn: 'root' })
export class ClienteService {
  private http = inject(HttpClient);
  private readonly url = `${API_URL}/clientes`;

  /** Lista paginada; quando o filtro é informado usa o endpoint /clientes/nome/{nome}. */
  listar(filtro = '', page = 0, pageSize = 10): Observable<Pagina<Cliente>> {
    const params = paginacao(page, pageSize);
    const termo = filtro.trim();
    const url = termo ? `${this.url}/nome/${encodeURIComponent(termo)}` : this.url;
    return this.http.get<Pagina<Cliente>>(url, { params });
  }

  buscarPorId(id: number): Observable<Cliente> {
    return this.http.get<Cliente>(`${this.url}/${id}`);
  }

  incluir(dados: ClienteRequest): Observable<Cliente> {
    return this.http.post<Cliente>(this.url, dados);
  }

  alterar(id: number, dados: ClienteRequest): Observable<Cliente> {
    return this.http.put<Cliente>(`${this.url}/${id}`, dados);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
