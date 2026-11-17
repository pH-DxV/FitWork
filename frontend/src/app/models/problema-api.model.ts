/** Formato de erro do back-end (RFC 7807 - application/problem+json). */
export interface ErroCampo {
  field: string;
  message: string;
}

export interface ProblemaApi {
  type?: string;
  title: string;
  status: number;
  detail?: string;
  instance?: string;
  timestamp?: string;
  errors?: ErroCampo[];
}
