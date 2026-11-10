import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api.config';
import { Categoria, CategoriaRequest } from '../models/categoria.model';
import { Pagina } from '../models/pagina.model';
import { paginacao } from './http-params';

@Injectable({ providedIn: 'root' })
export class CategoriaService {
  private http = inject(HttpClient);
  private readonly url = `${API_URL}/categorias`;

  /** Lista paginada; quando o filtro é informado usa o endpoint /categorias/nome/{nome}. */
  listar(filtro = '', page = 0, pageSize = 10): Observable<Pagina<Categoria>> {
    const params = paginacao(page, pageSize);
    const termo = filtro.trim();
    const url = termo ? `${this.url}/nome/${encodeURIComponent(termo)}` : this.url;
    return this.http.get<Pagina<Categoria>>(url, { params });
  }

  buscarPorId(id: number): Observable<Categoria> {
    return this.http.get<Categoria>(`${this.url}/${id}`);
  }

  incluir(dados: CategoriaRequest): Observable<Categoria> {
    return this.http.post<Categoria>(this.url, dados);
  }

  alterar(id: number, dados: CategoriaRequest): Observable<Categoria> {
    return this.http.put<Categoria>(`${this.url}/${id}`, dados);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
