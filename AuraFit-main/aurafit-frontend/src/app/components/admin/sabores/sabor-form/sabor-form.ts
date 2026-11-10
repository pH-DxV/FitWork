import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { Observable } from 'rxjs';
import { SaborRequest } from '../../../../models/sabor.model';
import { SaborService } from '../../../../services/sabor.service';
import { aplicarErrosDaApi, mensagemErro } from '../../../../shared/erros-formulario';
import { NotificacaoService } from '../../../../shared/notificacao.service';
import { Validadores } from '../../../../shared/validadores';

@Component({
  selector: 'app-sabor-form',
  imports: [ReactiveFormsModule, RouterLink, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule],
  templateUrl: './sabor-form.html',
})
export class SaborForm implements OnInit {
  private fb = inject(FormBuilder);
  private saborService = inject(SaborService);
  private notificacao = inject(NotificacaoService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  id = signal<number | null>(null);
  carregando = signal(false);
  salvando = signal(false);
  erroGeral = signal('');

  form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(80), Validadores.naoSomenteEspacos]],
    descricao: ['', [Validators.maxLength(255)]],
  });

  mensagemErro = (campo: string) => mensagemErro(this.form.get(campo));

  ngOnInit(): void {
    const idDaUrl = this.route.snapshot.paramMap.get('id');
    if (!idDaUrl) return;

    this.id.set(Number(idDaUrl));
    this.carregando.set(true);
    this.saborService.buscarPorId(this.id()!).subscribe({
      next: (sabor) => {
        this.form.patchValue({ nome: sabor.nome, descricao: sabor.descricao ?? '' });
        this.carregando.set(false);
      },
      error: () => {
        this.erroGeral.set('Sabor não encontrado.');
        this.carregando.set(false);
      },
    });
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.salvando.set(true);
    this.erroGeral.set('');

    const dados: SaborRequest = this.form.getRawValue();
    const requisicao: Observable<unknown> = this.id()
      ? this.saborService.alterar(this.id()!, dados)
      : this.saborService.incluir(dados);

    requisicao.subscribe({
      next: () => {
        this.notificacao.sucesso(this.id() ? 'Sabor atualizado.' : 'Sabor cadastrado.');
        this.router.navigateByUrl('/admin/sabores');
      },
      error: (erro: HttpErrorResponse) => {
        this.erroGeral.set(aplicarErrosDaApi(this.form, erro));
        this.salvando.set(false);
      },
    });
  }
}
