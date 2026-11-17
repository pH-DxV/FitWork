import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { Observable } from 'rxjs';
import { Cliente, ClienteRequest, Telefone } from '../../../../models/cliente.model';
import { Estado } from '../../../../models/estado.model';
import { CepService } from '../../../../services/cep.service';
import { ClienteService } from '../../../../services/cliente.service';
import { EstadoService } from '../../../../services/estado.service';
import { aplicarErrosDaApi, mensagemErro } from '../../../../shared/erros-formulario';
import { formatarCep, formatarCpf, formatarNumeroTelefone, somenteNumeros } from '../../../../shared/formatos';
import { NotificacaoService } from '../../../../shared/notificacao.service';
import { Validadores } from '../../../../shared/validadores';

/** Mesma idade mínima validada no back-end (ClienteServiceImpl). */
const IDADE_MINIMA = 16;

/**
 * Cadastro de cliente com consulta de CEP (ViaCEP).
 * O endereço é salvo de forma estruturada: o back-end localiza (ou cadastra)
 * o Município e o Estado a partir da cidade, UF e código IBGE retornados pelo ViaCEP.
 */
@Component({
  selector: 'app-cliente-form',
  imports: [
    ReactiveFormsModule, RouterLink, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule,
    MatIconModule, MatCheckboxModule, MatSlideToggleModule, MatProgressSpinnerModule,
  ],
  templateUrl: './cliente-form.html',
  styleUrl: './cliente-form.css',
})
export class ClienteForm implements OnInit {
  private fb = inject(FormBuilder);
  private clienteService = inject(ClienteService);
  private estadoService = inject(EstadoService);
  private cepService = inject(CepService);
  private notificacao = inject(NotificacaoService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  readonly idadeMinima = IDADE_MINIMA;
  readonly generos = ['Feminino', 'Masculino', 'Outro', 'Prefiro não informar'];

  id = signal<number | null>(null);
  carregando = signal(false);
  salvando = signal(false);
  erroGeral = signal('');
  estados = signal<Estado[]>([]);

  // estado da consulta de CEP
  buscandoCep = signal(false);
  avisoCep = signal('');
  falhaRedeCep = signal(false);
  enderecoPeloCep = signal(false);
  private ultimoCepConsultado = '';

  form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(120), Validadores.naoSomenteEspacos]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(120)]],
    cpf: ['', [Validators.required, Validadores.cpf]],
    dataNascimento: ['', [Validators.required, Validadores.dataNascimento(IDADE_MINIMA)]],
    genero: [''],
    telefones: this.fb.array([this.criarTelefone()]),
    endereco: this.fb.nonNullable.group({
      cep: ['', [Validators.required, Validadores.cep]],
      logradouro: ['', [Validators.required, Validators.maxLength(120), Validadores.naoSomenteEspacos]],
      numero: ['', [Validators.required, Validators.maxLength(10), Validadores.naoSomenteEspacos]],
      complemento: ['', [Validators.maxLength(60)]],
      bairro: ['', [Validators.required, Validators.maxLength(80), Validadores.naoSomenteEspacos]],
      cidade: ['', [Validators.required, Validators.maxLength(80), Validadores.naoSomenteEspacos]],
      uf: ['', [Validators.required]],
      codigoIbge: [''],
    }),
    aceitaNovidades: [true],
    ativo: [true],
  });

  get telefones(): FormArray<FormGroup> {
    return this.form.controls.telefones as FormArray<FormGroup>;
  }

  get endereco() {
    return this.form.controls.endereco;
  }

  mensagemErro = (caminho: string) => mensagemErro(this.form.get(caminho));

  ngOnInit(): void {
    this.estadoService.listarTodos().subscribe({
      next: (lista) => this.estados.set(lista),
      error: () => this.erroGeral.set('Não foi possível carregar os estados. O back-end está rodando?'),
    });

    const idDaUrl = this.route.snapshot.paramMap.get('id');
    if (!idDaUrl) return;

    this.id.set(Number(idDaUrl));
    this.carregando.set(true);
    this.clienteService.buscarPorId(this.id()!).subscribe({
      next: (cliente) => {
        this.preencher(cliente);
        this.carregando.set(false);
      },
      error: () => {
        this.erroGeral.set('Cliente não encontrado.');
        this.carregando.set(false);
      },
    });
  }

  // ---------- telefones ----------

  criarTelefone(telefone?: Telefone): FormGroup {
    return this.fb.nonNullable.group({
      codigoArea: [telefone?.codigoArea ?? '', [Validators.required, Validators.pattern(/^\d{2}$/)]],
      numero: [
        telefone ? formatarNumeroTelefone(telefone.numero) : '',
        [Validators.required, Validators.pattern(/^\d{4,5}-?\d{4}$/)],
      ],
      whatsapp: [telefone?.whatsapp ?? false],
    });
  }

  adicionarTelefone(): void {
    this.telefones.push(this.criarTelefone());
  }

  removerTelefone(indice: number): void {
    if (this.telefones.length > 1) this.telefones.removeAt(indice);
  }

  // ---------- CEP (ViaCEP) ----------

  buscarCep(forcar = false): void {
    const controle = this.endereco.controls.cep;
    if (controle.invalid) {
      controle.markAsTouched();
      return;
    }

    const cep = somenteNumeros(controle.value);
    if (this.buscandoCep() || (!forcar && cep === this.ultimoCepConsultado)) return;

    this.buscandoCep.set(true);
    this.avisoCep.set('');
    this.falhaRedeCep.set(false);

    this.cepService.consultar(cep).subscribe({
      next: (resposta) => {
        this.buscandoCep.set(false);
        this.ultimoCepConsultado = cep;
        controle.setValue(formatarCep(cep));

        if (!resposta) {
          this.avisoCep.set('CEP não encontrado. Confira o número ou preencha o endereço manualmente.');
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
        // cidade/UF vêm do ViaCEP: ficam travadas para manter o código IBGE coerente
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

  // ---------- máscaras ----------

  formatarCpf(): void {
    const controle = this.form.controls.cpf;
    if (somenteNumeros(controle.value).length === 11) controle.setValue(formatarCpf(controle.value));
  }

  formatarNumero(indice: number): void {
    const controle = this.telefones.at(indice).get('numero');
    const valor = somenteNumeros(controle?.value);
    if (controle && (valor.length === 8 || valor.length === 9)) controle.setValue(formatarNumeroTelefone(valor));
  }

  // ---------- salvar ----------

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.erroGeral.set('Confira os campos destacados.');
      return;
    }

    this.salvando.set(true);
    this.erroGeral.set('');

    const requisicao: Observable<unknown> = this.id()
      ? this.clienteService.alterar(this.id()!, this.toRequest())
      : this.clienteService.incluir(this.toRequest());

    requisicao.subscribe({
      next: () => {
        this.notificacao.sucesso(this.id() ? 'Cliente atualizado.' : 'Cliente cadastrado.');
        this.router.navigateByUrl('/admin/clientes');
      },
      error: (erro: HttpErrorResponse) => {
        this.erroGeral.set(aplicarErrosDaApi(this.form, erro));
        this.salvando.set(false);
      },
    });
  }

  private toRequest(): ClienteRequest {
    const valores = this.form.getRawValue();
    const endereco = valores.endereco;
    return {
      nome: valores.nome.trim(),
      email: valores.email.trim(),
      cpf: somenteNumeros(valores.cpf),
      dataNascimento: valores.dataNascimento,
      genero: valores.genero,
      telefones: (valores.telefones as Telefone[]).map((t) => ({
        codigoArea: somenteNumeros(t.codigoArea),
        numero: somenteNumeros(t.numero),
        whatsapp: t.whatsapp,
      })),
      endereco: {
        cep: somenteNumeros(endereco.cep),
        logradouro: endereco.logradouro.trim(),
        numero: endereco.numero.trim(),
        complemento: endereco.complemento.trim(),
        bairro: endereco.bairro.trim(),
        cidade: endereco.cidade.trim(),
        uf: endereco.uf,
        codigoIbge: endereco.codigoIbge || null,
      },
      aceitaNovidades: valores.aceitaNovidades,
      ativo: valores.ativo,
    };
  }

  private preencher(cliente: Cliente): void {
    this.telefones.clear();
    for (const telefone of cliente.telefones) this.telefones.push(this.criarTelefone(telefone));
    if (this.telefones.length === 0) this.adicionarTelefone();

    const municipio = cliente.endereco.municipio;
    this.form.patchValue({
      nome: cliente.nome,
      email: cliente.email,
      cpf: formatarCpf(cliente.cpf),
      dataNascimento: cliente.dataNascimento,
      genero: cliente.genero ?? '',
      endereco: {
        cep: formatarCep(cliente.endereco.cep),
        logradouro: cliente.endereco.logradouro,
        numero: cliente.endereco.numero,
        complemento: cliente.endereco.complemento ?? '',
        bairro: cliente.endereco.bairro,
        cidade: municipio.nome,
        uf: municipio.estado.sigla,
        codigoIbge: municipio.codigoIbge ?? '',
      },
      aceitaNovidades: cliente.aceitaNovidades,
      ativo: cliente.ativo,
    });
    this.ultimoCepConsultado = cliente.endereco.cep;
    if (municipio.codigoIbge) {
      this.endereco.controls.cidade.disable();
      this.endereco.controls.uf.disable();
      this.enderecoPeloCep.set(true);
    }
  }
}
