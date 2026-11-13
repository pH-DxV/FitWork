import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { SessaoService } from '../../../services/sessao.service';
import { mensagemErro } from '../../../shared/erros-formulario';
import { NotificacaoService } from '../../../shared/notificacao.service';

/**
 * Protótipo da tela de login. A autenticação com Usuário/JWT ainda não existe no
 * back-end; por enquanto qualquer e-mail válido com senha de 6+ caracteres entra.
 */
@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private fb = inject(FormBuilder);
  private sessao = inject(SessaoService);
  private notificacao = inject(NotificacaoService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  mostrarSenha = signal(false);
  modoCadastro = signal(false);

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    senha: ['', [Validators.required, Validators.minLength(6)]],
  });

  mensagemErro = (campo: string) => mensagemErro(this.form.get(campo));

  alternarModo(): void {
    this.modoCadastro.set(!this.modoCadastro());
  }

  esqueciSenha(): void {
    this.notificacao.sucesso('Recuperação de senha disponível após integração com JWT.');
  }

  entrar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.sessao.entrar(this.form.controls.email.value.trim());
    const retorno = this.route.snapshot.queryParamMap.get('retorno') ?? '/loja/perfil';
    this.router.navigateByUrl(retorno);
  }
}
