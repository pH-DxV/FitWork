import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { API_URL } from '../api.config';
import { ProblemaApi } from '../models/problema-api.model';
import { SessaoService } from '../services/sessao.service';
import { NotificacaoService } from '../shared/notificacao.service';

const ROTAS_PUBLICAS_AUTH = ['/auth/login', '/auth/cadastro', '/auth/refresh', '/auth/logout'];

/**
 * Interceptor HTTP de segurança:
 * 1. Anexa o cabeçalho Authorization: Bearer <token> EXCLUSIVAMENTE às chamadas destinadas
 *    ao back-end Quarkus (API_URL), evitando vazamento de token para APIs externas como ViaCEP;
 * 2. Trata HTTP 401 (Unauthorized): tenta renovar o token uma vez via cookie HttpOnly (/auth/refresh)
 *    e, se expirado, limpa a sessão e redireciona para o login;
 * 3. Trata HTTP 403 (Forbidden): exibe aviso claro de permissão insuficiente e impede acesso indevido.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // Segurança: jamais enviar credenciais ou Bearer token para origens externas (ex.: ViaCEP)
  if (!req.url.startsWith(API_URL)) {
    return next(req);
  }

  const sessao = inject(SessaoService);
  const notificacao = inject(NotificacaoService);
  const router = inject(Router);

  const caminho = req.url.slice(API_URL.length);
  const ehRotaPublicaAuth = ROTAS_PUBLICAS_AUTH.some((rota) => caminho.startsWith(rota));
  const token = sessao.obterAccessToken();

  const requisicaoAutenticada =
    !ehRotaPublicaAuth && token
      ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : req;

  return next(requisicaoAutenticada).pipe(
    catchError((erro: unknown) => {
      if (!(erro instanceof HttpErrorResponse)) {
        return throwError(() => erro);
      }

      // 401 em recurso protegido: tenta renovar o access token via cookie HttpOnly uma única vez
      if (erro.status === 401 && !ehRotaPublicaAuth) {
        return sessao.renovarToken().pipe(
          switchMap((novoToken) =>
            next(req.clone({ setHeaders: { Authorization: `Bearer ${novoToken}` } })),
          ),
          catchError(() => {
            sessao.limparSessaoLocal();
            notificacao.erro('Sua sessão expirou ou você não está autenticado. Faça login para continuar.');
            redirecionarParaLogin(router);
            return throwError(() => erro);
          }),
        );
      }

      // 403 Forbidden: usuário autenticado, porém sem o papel exigido (ou tentativa de IDOR bloqueada)
      if (erro.status === 403 && !ehRotaPublicaAuth) {
        const problema = erro.error as ProblemaApi | null;
        const mensagem =
          problema?.detail ?? 'Acesso negado (403): você não possui permissão para realizar esta operação.';
        notificacao.erro(mensagem);
        if (router.url.startsWith('/admin') && !sessao.ehAdmin()) {
          router.navigateByUrl('/loja');
        }
      }

      return throwError(() => erro);
    }),
  );
};

function redirecionarParaLogin(router: Router): void {
  const urlAtual = router.url;
  if (urlAtual.startsWith('/loja/login')) {
    return;
  }
  router.navigate(['/loja/login'], { queryParams: { retorno: urlAtual } });
}
