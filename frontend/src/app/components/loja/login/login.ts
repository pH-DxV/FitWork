import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { ActivatedRoute, Router } from '@angular/router';
import { CadastroContaRequest } from '../../../models/auth.model';
import { Telefone } from '../../../models/cliente.model';
import { Estado } from '../../../models/estado.model';
import { CepService } from '../../../services/cep.service';
import { EstadoService } from '../../../services/estado.service';
import { SessaoService } from '../../../services/sessao.service';
import { aplicarErrosDaApi, mensagemDaApi, mensagemErro } from '../../../shared/erros-formulario';
import {
  formatarCep,
  formatarCpf,
  formatarNumeroTelefone,
  somenteNumeros,
} from '../../../shared/formatos';
import { NotificacaoService } from '../../../shared/notificacao.service';
import { Validadores } from '../../../shared/validadores';

const IDADE_MINIMA = 16;

/**
 * Tela de login e autocadastro integrada ao Keycloak por intermédio exclusivo do back-end Quarkus.
 * A senha digitada é encaminhada apenas ao Quarkus (/auth/login ou /auth/cadastro), que a valida
 * ou registra no Keycloak sem jamais armazená-la no banco de dados do e-commerce.
 */
@Component({
  selector: 'app-login',
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatCheckboxModule,
    MatSlideToggleModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly sessao = inject(SessaoService);
  private readonly estadoService = inject(EstadoService);
  private readonly cepService = inject(CepService);
  private readonly notificacao = inject(NotificacaoService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly idadeMinima = IDADE_MINIMA;
  readonly generos = ['Feminino', 'Masculino', 'Outro', 'Prefiro não informar'];

  mostrarSenha = signal(false);
  modoCadastro = signal(false);
  processando = signal(false);
  erroGeral = signal('');
  estados = signal<Estado[]>([]);

  // estado da consulta ViaCEP no autocadastro
  buscandoCep = signal(false);
  avisoCep = signal('');
  falhaRedeCep = signal(false);
  enderecoPeloCep = signal(false);
  private ultimoCepConsultado = '';

  formLogin = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email, Validators.maxLength(120)]],
    senha: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(64)]],
  });

  formCadastro = this.fb.nonNullable.group({
    nome: [
      '',
      [
        Validators.required,
        Validators.minLength(2),
        Validators.maxLength(120),
        Validadores.naoSomenteEspacos,
      ],
    ],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(120)]],
    senha: [
      '',
      [
        Validators.required,
        Validators.minLength(8),
        Validators.maxLength(64),
        Validadores.senhaForte,
      ],
    ],
    confirmarSenha: ['', [Validators.required, Validadores.igualAoCampo('senha')]],
    cpf: ['', [Validators.required, Validadores.cpf]],
    dataNascimento: ['', [Validators.required, Validadores.dataNascimento(IDADE_MINIMA)]],
    genero: [''],
    telefones: this.fb.array([this.criarTelefone()]),
    endereco: this.fb.nonNullable.group({
      cep: ['', [Validators.required, Validadores.cep]],
      logradouro: [
        '',
        [Validators.required, Validators.maxLength(120), Validadores.naoSomenteEspacos],
      ],
      numero: ['', [Validators.required, Validators.maxLength(10), Validadores.naoSomenteEspacos]],
      complemento: ['', [Validators.maxLength(60)]],
      bairro: ['', [Validators.required, Validators.maxLength(80), Validadores.naoSomenteEspacos]],
      cidade: ['', [Validators.required, Validators.maxLength(80), Validadores.naoSomenteEspacos]],
      uf: ['', [Validators.required]],
      codigoIbge: [''],
    }),
    aceitaNovidades: [true],
  });

  get telefones(): FormArray<FormGroup> {
    return this.formCadastro.controls.telefones as FormArray<FormGroup>;
  }

  get endereco() {
    return this.formCadastro.controls.endereco;
  }

  ngOnInit(): void {
    this.estadoService.listarTodos().subscribe({
      next: (lista) => this.estados.set(lista),
      error: () => {
        // estados serão necessários apenas se o usuário preencher a UF manualmente
      },
    });

    this.formCadastro.controls.senha.valueChanges.subscribe(() => {
      this.formCadastro.controls.confirmarSenha.updateValueAndValidity({ emitEvent: false });
    });
  }

  mensagemErroLogin = (campo: string) => mensagemErro(this.formLogin.get(campo));
  mensagemErroCadastro = (caminho: string) => mensagemErro(this.formCadastro.get(caminho));

  alternarModo(): void {
    this.modoCadastro.set(!this.modoCadastro());
    this.erroGeral.set('');
  }

  preencherCredencialDemo(email: string, senha: string): void {
    this.modoCadastro.set(false);
    this.erroGeral.set('');
    this.formLogin.setValue({ email, senha });
  }

  entrar(): void {
    if (this.formLogin.invalid) {
      this.formLogin.markAllAsTouched();
      return;
    }

    this.processando.set(true);
    this.erroGeral.set('');

    const { email, senha } = this.formLogin.getRawValue();
    this.sessao.entrar({ email: email.trim(), senha }).subscribe({
      next: (usuario) => {
        this.processando.set(false);
        this.notificacao.sucesso(`Bem-vindo(a), ${usuario.nome}!`);
        this.navegarAposAutenticacao(usuario.perfis.includes('ADMIN'));
      },
      error: (erro: HttpErrorResponse) => {
        this.processando.set(false);
        this.erroGeral.set(
          mensagemDaApi(erro, 'Não foi possível autenticar. Verifique seu e-mail e senha.'),
        );
      },
    });
  }

  cadastrar(): void {
    if (this.formCadastro.invalid) {
      this.formCadastro.markAllAsTouched();
      this.erroGeral.set('Confira os campos destacados antes de concluir o cadastro.');
      return;
    }

    this.processando.set(true);
    this.erroGeral.set('');

    this.sessao.cadastrar(this.toCadastroRequest()).subscribe({
      next: (usuario) => {
        this.processando.set(false);
        this.notificacao.sucesso(`Conta criada com sucesso! Bem-vindo(a), ${usuario.nome}.`);
        this.navegarAposAutenticacao(false);
      },
      error: (erro: HttpErrorResponse) => {
        this.processando.set(false);
        this.erroGeral.set(aplicarErrosDaApi(this.formCadastro, erro));
      },
    });
  }

  // ---------- telefones e máscaras do autocadastro ----------

  criarTelefone(telefone?: Telefone): FormGroup {
    return this.fb.nonNullable.group({
      codigoArea: [
        telefone?.codigoArea ?? '',
        [Validators.required, Validators.pattern(/^\d{2}$/)],
      ],
      numero: [
        telefone ? formatarNumeroTelefone(telefone.numero) : '',
        [Validators.required, Validators.pattern(/^\d{4,5}-?\d{4}$/)],
      ],
      whatsapp: [telefone?.whatsapp ?? true],
    });
  }

  adicionarTelefone(): void {
    this.telefones.push(this.criarTelefone());
  }

  removerTelefone(indice: number): void {
    if (this.telefones.length > 1) {
      this.telefones.removeAt(indice);
    }
  }

  formatarCpf(): void {
    const controle = this.formCadastro.controls.cpf;
    if (somenteNumeros(controle.value).length === 11) {
      controle.setValue(formatarCpf(controle.value));
    }
  }

  formatarNumero(indice: number): void {
    const controle = this.telefones.at(indice).get('numero');
    const valor = somenteNumeros(controle?.value);
    if (controle && (valor.length === 8 || valor.length === 9)) {
      controle.setValue(formatarNumeroTelefone(valor));
    }
  }

  // ---------- consulta de CEP (ViaCEP) ----------

  buscarCep(forcar = false): void {
    const controle = this.endereco.controls.cep;
    if (controle.invalid) {
      controle.markAsTouched();
      return;
    }

    const cep = somenteNumeros(controle.value);
    if (this.buscandoCep() || (!forcar && cep === this.ultimoCepConsultado)) {
      return;
    }

    this.buscandoCep.set(true);
    this.avisoCep.set('');
    this.falhaRedeCep.set(false);

    this.cepService.consultar(cep).subscribe({
      next: (resposta) => {
        this.buscandoCep.set(false);
        this.ultimoCepConsultado = cep;
        controle.setValue(formatarCep(cep));

        if (!resposta) {
          this.avisoCep.set(
            'CEP não encontrado. Confira o número ou preencha o endereço manualmente.',
          );
          this.liberarCidadeUf();
          return;
        }

        this.endereco.patchValue({
          logradouro: resposta.logradouro || this.endereco.controls.logradouro.value,
          bairro: resposta.bairro || this.endereco.controls.bairro.value,
          cidade: resposta.localidade,
          uf: resposta.uf,
          codigoIbge: resposta.ibge,
          complemento: this.endereco.controls.complemento.value || resposta.complemento,
        });
        this.endereco.controls.cidade.disable();
        this.endereco.controls.uf.disable();
        this.enderecoPeloCep.set(true);
      },
      error: () => {
        this.buscandoCep.set(false);
        this.falhaRedeCep.set(true);
        this.liberarCidadeUf();
      },
    });
  }

  liberarCidadeUf(): void {
    this.endereco.controls.cidade.enable();
    this.endereco.controls.uf.enable();
    this.endereco.controls.codigoIbge.setValue('');
    this.enderecoPeloCep.set(false);
  }

  private toCadastroRequest(): CadastroContaRequest {
    const valores = this.formCadastro.getRawValue();
    const end = valores.endereco;
    return {
      nome: valores.nome.trim(),
      email: valores.email.trim(),
      senha: valores.senha,
      cpf: somenteNumeros(valores.cpf),
      dataNascimento: valores.dataNascimento,
      genero: valores.genero,
      telefones: (valores.telefones as Telefone[]).map((t) => ({
        codigoArea: somenteNumeros(t.codigoArea),
        numero: somenteNumeros(t.numero),
        whatsapp: t.whatsapp,
      })),
      endereco: {
        cep: somenteNumeros(end.cep),
        logradouro: end.logradouro.trim(),
        numero: end.numero.trim(),
        complemento: end.complemento.trim(),
        bairro: end.bairro.trim(),
        cidade: end.cidade.trim(),
        uf: end.uf,
        codigoIbge: end.codigoIbge || null,
      },
      aceitaNovidades: valores.aceitaNovidades,
    };
  }

  /**
   * Prevenção contra Open Redirect: aceita apenas caminhos internos iniciados por "/" (e não "//").
   */
  private navegarAposAutenticacao(ehAdmin: boolean): void {
    const retornoBruto = this.route.snapshot.queryParamMap.get('retorno');
    const retornoSeguro =
      retornoBruto && retornoBruto.startsWith('/') && !retornoBruto.startsWith('//')
        ? retornoBruto
        : null;

    if (retornoSeguro) {
      this.router.navigateByUrl(retornoSeguro);
      return;
    }
    this.router.navigateByUrl(ehAdmin ? '/admin' : '/loja/perfil');
  }
}
