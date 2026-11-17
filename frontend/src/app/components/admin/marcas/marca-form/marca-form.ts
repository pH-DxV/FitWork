import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { Observable } from 'rxjs';
import { MarcaRequest } from '../../../../models/marca.model';
import { MarcaService } from '../../../../services/marca.service';
import { aplicarErrosDaApi, mensagemErro } from '../../../../shared/erros-formulario';
import { formatarCnpj, somenteNumeros } from '../../../../shared/formatos';
import { NotificacaoService } from '../../../../shared/notificacao.service';
import { Validadores } from '../../../../shared/validadores';

@Component({
  selector: 'app-marca-form',
  imports: [
    ReactiveFormsModule, RouterLink, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule,
    MatSlideToggleModule,
  ],
  templateUrl: './marca-form.html',
})
export class MarcaForm implements OnInit {
  private fb = inject(FormBuilder);
  private marcaService = inject(MarcaService);
  private notificacao = inject(NotificacaoService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  id = signal<number | null>(null);
  carregando = signal(false);
  salvando = signal(false);
  erroGeral = signal('');

  form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100), Validadores.naoSomenteEspacos]],
    cnpj: ['', [Validators.required, Validadores.cnpj]],
    emailContato: ['', [Validators.email, Validators.maxLength(100)]],
    ativa: [true],
  });

  mensagemErro = (campo: string) => mensagemErro(this.form.get(campo));

  ngOnInit(): void {
    const idDaUrl = this.route.snapshot.paramMap.get('id');
    if (!idDaUrl) return;

    this.id.set(Number(idDaUrl));
    this.carregando.set(true);
    this.marcaService.buscarPorId(this.id()!).subscribe({
      next: (marca) => {
        this.form.patchValue({
          nome: marca.nome,
          cnpj: formatarCnpj(marca.cnpj),
          emailContato: marca.emailContato ?? '',
          ativa: marca.ativa,
        });
        this.carregando.set(false);
      },
      error: () => {
        this.erroGeral.set('Marca não encontrada.');
        this.carregando.set(false);
      },
    });
  }

  /** Aplica a máscara 00.000.000/0000-00 ao sair do campo. */
  formatarCnpj(): void {
    const controle = this.form.controls.cnpj;
    if (somenteNumeros(controle.value).length === 14) controle.setValue(formatarCnpj(controle.value));
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.salvando.set(true);
    this.erroGeral.set('');

    const valores = this.form.getRawValue();
    const dados: MarcaRequest = {
      ...valores,
      nome: valores.nome.trim(),
      cnpj: somenteNumeros(valores.cnpj),
      emailContato: valores.emailContato.trim(),
    };
    const requisicao: Observable<unknown> = this.id()
      ? this.marcaService.alterar(this.id()!, dados)
      : this.marcaService.incluir(dados);

    requisicao.subscribe({
      next: () => {
        this.notificacao.sucesso(this.id() ? 'Marca atualizada.' : 'Marca cadastrada.');
        this.router.navigateByUrl('/admin/marcas');
      },
      error: (erro: HttpErrorResponse) => {
        this.erroGeral.set(aplicarErrosDaApi(this.form, erro));
        this.salvando.set(false);
      },
    });
  }
}
