import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { Observable } from 'rxjs';
import { FornecedorRequest } from '../../../../models/fornecedor.model';
import { FornecedorService } from '../../../../services/fornecedor.service';
import { aplicarErrosDaApi, mensagemErro } from '../../../../shared/erros-formulario';
import { formatarCnpj, formatarTelefone, somenteNumeros } from '../../../../shared/formatos';
import { NotificacaoService } from '../../../../shared/notificacao.service';
import { Validadores } from '../../../../shared/validadores';

@Component({
  selector: 'app-fornecedor-form',
  imports: [
    ReactiveFormsModule, RouterLink, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule,
  ],
  templateUrl: './fornecedor-form.html',
})
export class FornecedorForm implements OnInit {
  private fb = inject(FormBuilder);
  private fornecedorService = inject(FornecedorService);
  private notificacao = inject(NotificacaoService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  id = signal<number | null>(null);
  carregando = signal(false);
  salvando = signal(false);
  erroGeral = signal('');

  form = this.fb.nonNullable.group({
    razaoSocial: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(120), Validadores.naoSomenteEspacos]],
    cnpj: ['', [Validators.required, Validadores.cnpj]],
    telefone: ['', [Validators.required, Validadores.telefone]],
    email: ['', [Validators.email, Validators.maxLength(100)]],
  });

  mensagemErro = (campo: string) => mensagemErro(this.form.get(campo));

  ngOnInit(): void {
    const idDaUrl = this.route.snapshot.paramMap.get('id');
    if (!idDaUrl) return;

    this.id.set(Number(idDaUrl));
    this.carregando.set(true);
    this.fornecedorService.buscarPorId(this.id()!).subscribe({
      next: (fornecedor) => {
        this.form.patchValue({
          razaoSocial: fornecedor.razaoSocial,
          cnpj: formatarCnpj(fornecedor.cnpj),
          telefone: formatarTelefone(fornecedor.telefone),
          email: fornecedor.email ?? '',
        });
        this.carregando.set(false);
      },
      error: () => {
        this.erroGeral.set('Fornecedor não encontrado.');
        this.carregando.set(false);
      },
    });
  }

  /** Aplica a máscara 00.000.000/0000-00 ao sair do campo. */
  formatarCnpj(): void {
    const controle = this.form.controls.cnpj;
    if (somenteNumeros(controle.value).length === 14) controle.setValue(formatarCnpj(controle.value));
  }

  /** Aplica a máscara (00) 00000-0000 ao sair do campo. */
  formatarTelefone(): void {
    const controle = this.form.controls.telefone;
    const tamanho = somenteNumeros(controle.value).length;
    if (tamanho === 10 || tamanho === 11) controle.setValue(formatarTelefone(controle.value));
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.salvando.set(true);
    this.erroGeral.set('');

    const valores = this.form.getRawValue();
    const dados: FornecedorRequest = {
      ...valores,
      razaoSocial: valores.razaoSocial.trim(),
      cnpj: somenteNumeros(valores.cnpj),
      telefone: somenteNumeros(valores.telefone),
      email: valores.email.trim(),
    };
    const requisicao: Observable<unknown> = this.id()
      ? this.fornecedorService.alterar(this.id()!, dados)
      : this.fornecedorService.incluir(dados);

    requisicao.subscribe({
      next: () => {
        this.notificacao.sucesso(this.id() ? 'Fornecedor atualizado.' : 'Fornecedor cadastrado.');
        this.router.navigateByUrl('/admin/fornecedores');
      },
      error: (erro: HttpErrorResponse) => {
        this.erroGeral.set(aplicarErrosDaApi(this.form, erro));
        this.salvando.set(false);
      },
    });
  }
}
