import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api.config';
import { Marca, MarcaRequest } from '../models/marca.model';
import { Pagina } from '../models/pagina.model';
import { paginacao, carregarTodasPaginas } from './http-params';

@Injectable({ providedIn: 'root' })
export class MarcaService {
  private http = inject(HttpClient);
  private readonly url = `${API_URL}/marcas`;

  /** Lista paginada com filtro opcional via query parameter (?nome=). */
  listar(filtro = '', page = 0, pageSize = 10): Observable<Pagina<Marca>> {
    let params = paginacao(page, pageSize);
    const termo = filtro.trim();
    if (termo) {
      params = params.set('nome', termo);
    }
    return this.http.get<Pagina<Marca>>(this.url, { params });
  }

  listarTodos(): Observable<Marca[]> {
    return carregarTodasPaginas((page, pageSize) => this.listar('', page, pageSize));
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
