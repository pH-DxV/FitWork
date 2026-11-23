import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';
import { SessaoService } from '../services/sessao.service';
import { NotificacaoService } from '../shared/notificacao.service';

/**
 * Exige que o usuário esteja autenticado (qualquer perfil válido: CLIENTE ou ADMIN).
 * Se a página foi recarregada (F5), tenta restaurar a sessão via cookie HttpOnly antes de bloquear.
 */
export const authGuard: CanActivateFn = (_route, state) => {
  const sessao = inject(SessaoService);
  const router = inject(Router);
  const notificacao = inject(NotificacaoService);

  return sessao.garantirSessaoRestaurada().pipe(
    map((autenticado) => {
      if (autenticado) {
        return true;
      }
      notificacao.erro('Faça login para acessar esta página.');
      return router.createUrlTree(['/loja/login'], { queryParams: { retorno: state.url } });
    }),
  );
};

/**
 * Exige que o usuário esteja autenticado E possua o papel ADMIN atribuído no Keycloak.
 */
export const adminGuard: CanActivateFn = (_route, state) => {
  const sessao = inject(SessaoService);
  const router = inject(Router);
  const notificacao = inject(NotificacaoService);

  return sessao.garantirSessaoRestaurada().pipe(
    map((autenticado) => {
      if (!autenticado) {
        notificacao.erro('Faça login com uma credencial administrativa para acessar o painel.');
        return router.createUrlTree(['/loja/login'], { queryParams: { retorno: state.url } });
      }
      if (!sessao.ehAdmin()) {
        notificacao.erro('Acesso negado (403): área restrita a administradores.');
        return router.createUrlTree(['/loja']);
      }
      return true;
    }),
  );
};
