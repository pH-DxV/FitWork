import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { Observable } from 'rxjs';
import { Cliente, ClienteRequest, Telefone } from '../../../../models/cliente.model';
import { Estado } from '../../../../models/estado.model';
import { CepService } from '../../../../services/cep.service';
import { ClienteService } from '../../../../services/cliente.service';
import { EstadoService } from '../../../../services/estado.service';
import { confirmar } from '../../../../shared/confirmacao-dialog';
import { aplicarErrosDaApi, mensagemDaApi, mensagemErro } from '../../../../shared/erros-formulario';
import { FormatoPipe } from '../../../../shared/formato.pipe';
import { formatarCep, formatarCpf, formatarNumeroTelefone, somenteNumeros } from '../../../../shared/formatos';
import { NotificacaoService } from '../../../../shared/notificacao.service';
import { Validadores } from '../../../../shared/validadores';

const IDADE_MINIMA = 16;

@Component({
  selector: 'app-cliente-list',
  imports: [
    RouterLink,
    ReactiveFormsModule,
    FormatoPipe,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
  ],
  templateUrl: './cliente-list.html',
})
export class ClienteList implements OnInit {
  private fb = inject(FormBuilder);
  private clienteService = inject(ClienteService);
  private estadoService = inject(EstadoService);
  private cepService = inject(CepService);
  private dialog = inject(MatDialog);
  private notificacao = inject(NotificacaoService);
  private temporizadorBusca?: ReturnType<typeof setTimeout>;

  readonly idadeMinima = IDADE_MINIMA;
  readonly generos = ['Feminino', 'Masculino', 'Outro', 'Prefiro não informar'];

  colunas = ['nome', 'cpf', 'telefone', 'cidade', 'situacao', 'acoes'];

  clientes = signal<Cliente[]>([]);
  estados = signal<Estado[]>([]);
  totalItems = signal(0);
  pageIndex = signal(0);
  pageSize = signal(10);
  filtro = signal('');
  filtroUf = signal('');
  filtroRegiao = signal('');
  filtroStatus = signal('');
  carregando = signal(false);
  erro = signal('');

  // Slide-over Drawer (Stitch 13_admin_clientes.html)
  drawerAberto = signal(false);
  editandoId = signal<number | null>(null);
  salvando = signal(false);
  erroFormulario = signal('');
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

  clientesFiltrados = computed(() => {
    const uf = this.filtroUf();
    const regiao = this.filtroRegiao().toLowerCase();
    const status = this.filtroStatus();

    return this.clientes().filter((c) => {
      const matchUf = !uf || c.endereco?.municipio?.estado?.sigla === uf;
      const matchRegiao =
        !regiao || (c.endereco?.municipio?.estado?.regiao?.nome || '').toLowerCase().includes(regiao);
      const matchStatus =
        !status || (status === 'ATIVO' && c.ativo) || (status === 'INATIVO' && !c.ativo);
      return matchUf && matchRegiao && matchStatus;
    });
  });

  clientesAtivosCount = computed(() => this.clientes().filter((c) => c.ativo).length);
  clientesOptInCount = computed(() => this.clientes().filter((c) => c.aceitaNovidades).length);
  totalTelefonesCount = computed(() =>
    this.clientes().reduce((acc, c) => acc + (c.telefones?.length || 0), 0),
  );
  whatsappCount = computed(() =>
    this.clientes().reduce(
      (acc, c) => acc + (c.telefones?.filter((t) => t.whatsapp)?.length || 0),
      0,
    ),
  );

  ngOnInit(): void {
    this.carregar();
    this.estadoService.listarTodos().subscribe({
      next: (lista) => this.estados.set(lista),
      error: () => {},
    });
  }

  carregar(): void {
    this.carregando.set(true);
    this.erro.set('');

    this.clienteService.listar(this.filtro(), this.pageIndex(), this.pageSize()).subscribe({
      next: (pagina) => {
        this.clientes.set(pagina.items);
        this.totalItems.set(pagina.totalItems);
        this.carregando.set(false);
      },
      error: (erro: HttpErrorResponse) => {
        this.erro.set(mensagemDaApi(erro, 'Não foi possível carregar os clientes.'));
        this.carregando.set(false);
      },
    });
  }

  buscar(texto: string): void {
    clearTimeout(this.temporizadorBusca);
    this.temporizadorBusca = setTimeout(() => {
      this.filtro.set(texto);
      this.pageIndex.set(0);
      this.carregar();
    }, 300);
  }

  mudarPagina(evento: PageEvent): void {
    this.pageIndex.set(evento.pageIndex);
    this.pageSize.set(evento.pageSize);
    this.carregar();
  }

  iniciaisCliente(nome: string): string {
    const partes = (nome || 'CL').trim().split(/\s+/);
    if (partes.length >= 2) {
      return (partes[0][0] + partes[partes.length - 1][0]).toUpperCase();
    }
    return (nome || 'CL').slice(0, 2).toUpperCase();
  }

  telefonePrincipal(cliente: Cliente): string {
    const telefone = cliente.telefones[0];
    return telefone ? telefone.codigoArea + telefone.numero : '';
  }

  temWhatsappPrincipal(cliente: Cliente): boolean {
    return Boolean(cliente.telefones?.[0]?.whatsapp);
  }

  // ---------- Drawer Lateral ----------

  abrirNovoClienteDrawer(): void {
    this.editandoId.set(null);
    this.erroFormulario.set('');
    this.avisoCep.set('');
    this.falhaRedeCep.set(false);
    this.ultimoCepConsultado = '';
    this.telefones.clear();
    this.telefones.push(this.criarTelefone());
    this.liberarCidadeUf();
    this.form.reset({
      nome: '',
      email: '',
      cpf: '',
      dataNascimento: '',
      genero: '',
      endereco: {
        cep: '',
        logradouro: '',
        numero: '',
        complemento: '',
        bairro: '',
        cidade: '',
        uf: '',
        codigoIbge: '',
      },
      aceitaNovidades: true,
      ativo: true,
    });
    this.drawerAberto.set(true);
  }

  abrirEdicaoDrawer(cliente: Cliente): void {
    this.editandoId.set(cliente.id);
    this.erroFormulario.set('');
    this.avisoCep.set('');
    this.falhaRedeCep.set(false);
    this.preencherFormulario(cliente);
    this.drawerAberto.set(true);
  }

  fecharDrawer(): void {
    this.drawerAberto.set(false);
  }

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

  formatarCpfInput(): void {
    const controle = this.form.controls.cpf;
    if (somenteNumeros(controle.value).length === 11) controle.setValue(formatarCpf(controle.value));
  }

  formatarNumero(indice: number): void {
    const controle = this.telefones.at(indice).get('numero');
    const valor = somenteNumeros(controle?.value);
    if (controle && (valor.length === 8 || valor.length === 9)) controle.setValue(formatarNumeroTelefone(valor));
  }

  salvarDrawer(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.erroFormulario.set('Confira os campos destacados.');
      return;
    }

    this.salvando.set(true);
    this.erroFormulario.set('');

    const idAtual = this.editandoId();
    const requisicao: Observable<unknown> = idAtual
      ? this.clienteService.alterar(idAtual, this.toRequest())
      : this.clienteService.incluir(this.toRequest());

    requisicao.subscribe({
      next: () => {
        this.notificacao.sucesso(idAtual ? 'Cliente atualizado.' : 'Cliente cadastrado.');
        this.salvando.set(false);
        this.drawerAberto.set(false);
        this.carregar();
      },
      error: (erro: HttpErrorResponse) => {
        this.erroFormulario.set(aplicarErrosDaApi(this.form, erro));
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

  private preencherFormulario(cliente: Cliente): void {
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
    } else {
      this.liberarCidadeUf();
    }
  }

  exportarRelatorio(): void {
    this.notificacao.sucesso('Relatório da base de clientes preparado para exportação.');
  }

  excluir(cliente: Cliente): void {
    confirmar(this.dialog, {
      titulo: 'Excluir cliente',
      mensagem: `Deseja excluir o cliente "${cliente.nome}"? Os telefones e o endereço também serão removidos.`,
    }).subscribe((confirmou) => {
      if (!confirmou) return;

      this.clienteService.excluir(cliente.id).subscribe({
        next: () => {
          this.notificacao.sucesso('Cliente excluído.');
          if (this.editandoId() === cliente.id) this.fecharDrawer();
          if (this.clientes().length === 1 && this.pageIndex() > 0) this.pageIndex.update((p) => p - 1);
          this.carregar();
        },
        error: (erro: HttpErrorResponse) =>
          this.notificacao.erro(mensagemDaApi(erro, 'Não foi possível excluir o cliente.')),
      });
    });
  }
}
