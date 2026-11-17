import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { API_URL } from '../api.config';
import { Estado } from '../models/estado.model';
import { Pagina } from '../models/pagina.model';
import { paginacao } from './http-params';

@Injectable({ providedIn: 'root' })
export class EstadoService {
  private http = inject(HttpClient);

  /** Retorna os 27 estados (usado no select de UF). */
  listarTodos(): Observable<Estado[]> {
    return this.http
      .get<Pagina<Estado>>(`${API_URL}/estados`, { params: paginacao(0, 30) })
      .pipe(map((pagina) => pagina.items));
  }
}
