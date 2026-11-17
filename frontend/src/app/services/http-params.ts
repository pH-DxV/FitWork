import { HttpParams } from '@angular/common/http';
import { EMPTY, Observable, expand, reduce } from 'rxjs';
import { Pagina } from '../models/pagina.model';

/** Monta os parâmetros de paginação (?page=&pageSize=) usados por todas as listagens. */
export function paginacao(page: number, pageSize: number): HttpParams {
  return new HttpParams().set('page', page).set('pageSize', pageSize);
}

/** Percorre todas as páginas de um endpoint paginado e devolve todos os itens acumulados. */
export function carregarTodasPaginas<T>(
  buscarPagina: (page: number, pageSize: number) => Observable<Pagina<T>>,
  pageSize = 100,
): Observable<T[]> {
  return buscarPagina(0, pageSize).pipe(
    expand((pagina) => (pagina.page + 1 < pagina.totalPages ? buscarPagina(pagina.page + 1, pageSize) : EMPTY)),
    reduce((acumulado, pagina) => [...acumulado, ...pagina.items], [] as T[]),
  );
}
