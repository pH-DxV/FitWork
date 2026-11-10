/** Formato do PageResponse<T> devolvido pelo back-end. */
export interface Pagina<T> {
  items: T[];
  page: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
}
