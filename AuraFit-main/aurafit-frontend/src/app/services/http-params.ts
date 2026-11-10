import { HttpParams } from '@angular/common/http';

/** Monta os parâmetros de paginação (?page=&pageSize=) usados por todas as listagens. */
export function paginacao(page: number, pageSize: number): HttpParams {
  return new HttpParams().set('page', page).set('pageSize', pageSize);
}
