import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { Observable } from 'rxjs';
import { CategoriaRequest } from '../../../../models/categoria.model';
import { CategoriaService } from '../../../../services/categoria.service';
import { aplicarErrosDaApi, mensagemErro } from '../../../../shared/erros-formulario';
import { NotificacaoService } from '../../../../shared/notificacao.service';
import { Validadores } from '../../../../shared/validadores';

@Component({
  selector: 'app-categoria-form',
  imports: [ReactiveFormsModule, RouterLink, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule],
  templateUrl: './categoria-form.html',
})
export class CategoriaForm implements OnInit {
  private fb = inject(FormBuilder);
  private categoriaService = inject(CategoriaService);
  private notificacao = inject(NotificacaoService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  id = signal<number | null>(null);
  carregando = signal(false);
  salvando = signal(false);
  erroGeral = signal('');

  form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100), Validadores.naoSomenteEspacos]],
    descricao: ['', [Validators.maxLength(255)]],
  });

  mensagemErro = (campo: string) => mensagemErro(this.form.get(campo));

  ngOnInit(): void {
    const idDaUrl = this.route.snapshot.paramMap.get('id');
    if (!idDaUrl) return;

    this.id.set(Number(idDaUrl));
    this.carregando.set(true);
    this.categoriaService.buscarPorId(this.id()!).subscribe({
      next: (categoria) => {
        this.form.patchValue({ nome: categoria.nome, descricao: categoria.descricao ?? '' });
        this.carregando.set(false);
      },
      error: () => {
        this.erroGeral.set('Categoria não encontrada.');
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

    const valores = this.form.getRawValue();
    const dados: CategoriaRequest = {
      nome: valores.nome.trim(),
      descricao: valores.descricao.trim(),
    };
    const requisicao: Observable<unknown> = this.id()
      ? this.categoriaService.alterar(this.id()!, dados)
      : this.categoriaService.incluir(dados);

    requisicao.subscribe({
      next: () => {
        this.notificacao.sucesso(this.id() ? 'Categoria atualizada.' : 'Categoria cadastrada.');
        this.router.navigateByUrl('/admin/categorias');
      },
      error: (erro: HttpErrorResponse) => {
        this.erroGeral.set(aplicarErrosDaApi(this.form, erro));
        this.salvando.set(false);
      },
    });
  }
}
