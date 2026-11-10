import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api.config';
import { Marca, MarcaRequest } from '../models/marca.model';
import { Pagina } from '../models/pagina.model';
import { paginacao } from './http-params';

@Injectable({ providedIn: 'root' })
export class MarcaService {
  private http = inject(HttpClient);
  private readonly url = `${API_URL}/marcas`;

  /** Lista paginada; quando o filtro é informado usa o endpoint /marcas/nome/{nome}. */
  listar(filtro = '', page = 0, pageSize = 10): Observable<Pagina<Marca>> {
    const params = paginacao(page, pageSize);
    const termo = filtro.trim();
    const url = termo ? `${this.url}/nome/${encodeURIComponent(termo)}` : this.url;
    return this.http.get<Pagina<Marca>>(url, { params });
  }

  buscarPorId(id: number): Observable<Marca> {
    return this.http.get<Marca>(`${this.url}/${id}`);
  }

  incluir(dados: MarcaRequest): Observable<Marca> {
    return this.http.post<Marca>(this.url, dados);
  }

  alterar(id: number, dados: MarcaRequest): Observable<Marca> {
    return this.http.put<Marca>(`${this.url}/${id}`, dados);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
