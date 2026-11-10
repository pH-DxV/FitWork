import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api.config';
import { Sabor, SaborRequest } from '../models/sabor.model';
import { Pagina } from '../models/pagina.model';
import { paginacao } from './http-params';

@Injectable({ providedIn: 'root' })
export class SaborService {
  private http = inject(HttpClient);
  private readonly url = `${API_URL}/sabores`;

  /** Lista paginada; quando o filtro é informado usa o endpoint /sabores/nome/{nome}. */
  listar(filtro = '', page = 0, pageSize = 10): Observable<Pagina<Sabor>> {
    const params = paginacao(page, pageSize);
    const termo = filtro.trim();
    const url = termo ? `${this.url}/nome/${encodeURIComponent(termo)}` : this.url;
    return this.http.get<Pagina<Sabor>>(url, { params });
  }

  buscarPorId(id: number): Observable<Sabor> {
    return this.http.get<Sabor>(`${this.url}/${id}`);
  }

  incluir(dados: SaborRequest): Observable<Sabor> {
    return this.http.post<Sabor>(this.url, dados);
  }

  alterar(id: number, dados: SaborRequest): Observable<Sabor> {
    return this.http.put<Sabor>(`${this.url}/${id}`, dados);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
