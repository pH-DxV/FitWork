import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api.config';
import { Pagina } from '../models/pagina.model';
import { FiltroProduto, Produto, ProdutoRequest } from '../models/produto.model';
import { TipoProteina } from '../models/tipo-proteina.model';
import { paginacao } from './http-params';

@Injectable({ providedIn: 'root' })
export class ProdutoService {
  private http = inject(HttpClient);
  private readonly url = `${API_URL}/produtos`;

  /** Listagem paginada com filtros opcionais (nome, tipo de proteína, categoria, marca e status ativo). */
  listar(filtro: FiltroProduto = {}, page = 0, pageSize = 10): Observable<Pagina<Produto>> {
    let params = paginacao(page, pageSize);
    if (filtro.nome?.trim()) params = params.set('nome', filtro.nome.trim());
    if (filtro.idTipoProteina) params = params.set('idTipoProteina', filtro.idTipoProteina);
    if (filtro.idCategoria) params = params.set('idCategoria', filtro.idCategoria);
    if (filtro.idMarca) params = params.set('idMarca', filtro.idMarca);
    if (filtro.ativo !== undefined && filtro.ativo !== null) params = params.set('ativo', filtro.ativo);
    return this.http.get<Pagina<Produto>>(this.url, { params });
  }

  buscarPorId(id: number): Observable<Produto> {
    return this.http.get<Produto>(`${this.url}/${id}`);
  }

  incluir(dados: ProdutoRequest): Observable<Produto> {
    return this.http.post<Produto>(this.url, dados);
  }

  alterar(id: number, dados: ProdutoRequest): Observable<Produto> {
    return this.http.put<Produto>(`${this.url}/${id}`, dados);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  uploadImagem(id: number, arquivo: File): Observable<Produto> {
    const formData = new FormData();
    formData.append('arquivo', arquivo, arquivo.name);
    return this.http.patch<Produto>(`${this.url}/${id}/imagem`, formData);
  }

  removerImagem(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}/imagem`);
  }

  urlImagem(produto: Produto | null | undefined, download = false): string | null {
    if (!produto?.imagemUrl) return null;
    const caminho = produto.imagemUrl.startsWith('http')
      ? produto.imagemUrl
      : `${API_URL}${produto.imagemUrl}`;
    const separador = caminho.includes('?') ? '&' : '?';
    const sufixoNome = produto.nomeImagem ? `v=${encodeURIComponent(produto.nomeImagem)}` : '';
    if (download) {
      return `${caminho}${separador}download=true`;
    }
    return sufixoNome ? `${caminho}${separador}${sufixoNome}` : caminho;
  }

  listarTiposProteina(): Observable<TipoProteina[]> {
    return this.http.get<TipoProteina[]>(`${API_URL}/tipos-proteina`);
  }
}
