import { CurrencyPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { CarrinhoService, ItemCarrinho } from '../../../services/carrinho.service';
import { CepService } from '../../../services/cep.service';
import { SessaoService } from '../../../services/sessao.service';
import { mensagemErro } from '../../../shared/erros-formulario';
import { formatarCep, somenteNumeros } from '../../../shared/formatos';
import { NotificacaoService } from '../../../shared/notificacao.service';
import { Validadores } from '../../../shared/validadores';

type FormaPagamento = 'pix' | 'cartao' | 'boleto';
type ModalidadeLogistica = 'rodonaves' | 'gollog';

/** Tela de Checkout B2B e Confirmação do Pedido (Google Stitch 02 & 03). */
@Component({
  selector: 'app-pagamento',
  imports: [
    CurrencyPipe,
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatRadioModule,
    MatButtonModule,
    MatIconModule,
  ],
  templateUrl: './pagamento.html',
  styleUrl: './pagamento.css',
})
export class Pagamento implements OnInit {
  private fb = inject(FormBuilder);
  private cepService = inject(CepService);
  sessao = inject(SessaoService);
  private notificacao = inject(NotificacaoService);
  private router = inject(Router);
  carrinho = inject(CarrinhoService);

  buscandoCep = signal(false);
  avisoCep = signal('');
  pedidoConcluido = signal<string | null>(null);
  totalPago = signal(0);
  subtotalFaturado = signal(0);
  freteFaturado = signal(0);
  descontoFaturado = signal(0);
  itensFaturados = signal<ItemCarrinho[]>([]);
  modalidadeLogistica = signal<ModalidadeLogistica>('rodonaves');
  dataEmissao = signal(new Date().toLocaleDateString('pt-BR'));

  readonly codigoBarrasBoleto = '34191.79001 01043.510047 91020.150008 8 98950000482500';
  readonly chaveAcessoNfe = '3525 0348 9123 0100 0192 5500 1000 0294 1810 0294 1894';

  form = this.fb.nonNullable.group({
    cep: ['', [Validators.required, Validadores.cep]],
    logradouro: ['', Validators.required],
    numero: ['', Validators.required],
    complemento: [''],
    bairro: ['', Validators.required],
    cidade: ['', Validators.required],
    uf: ['', Validators.required],
    forma: ['pix' as FormaPagamento],
    numeroCartao: [''],
    nomeCartao: [''],
    validade: [''],
    cvv: [''],
    parcelas: [1],
  });

  mensagemErro = (campo: string) => mensagemErro(this.form.get(campo));

  ngOnInit(): void {
    if (!this.sessao.usuario()) {
      this.router.navigate(['/loja/login'], { queryParams: { retorno: '/loja/pagamento' } });
      return;
    }
    const cliente = this.sessao.cliente();
    if (cliente?.endereco) {
      this.form.patchValue({
        cep: formatarCep(cliente.endereco.cep),
        logradouro: cliente.endereco.logradouro,
        numero: cliente.endereco.numero,
        complemento: cliente.endereco.complemento ?? '',
        bairro: cliente.endereco.bairro,
        cidade: cliente.endereco.municipio?.nome ?? '',
        uf: cliente.endereco.municipio?.estado?.sigla ?? '',
      });
    }
    this.form.controls.forma.valueChanges.subscribe((forma) => this.ajustarValidacaoCartao(forma));
  }

  parcelas(): number[] {
    return [1, 2, 3, 4, 5, 6];
  }

  selecionarForma(forma: FormaPagamento): void {
    this.form.controls.forma.setValue(forma);
  }

  selecionarLogistica(modalidade: ModalidadeLogistica): void {
    this.modalidadeLogistica.set(modalidade);
  }

  custoLogistica(): number {
    if (this.modalidadeLogistica() === 'gollog') {
      return 64.9;
    }
    return this.carrinho.frete();
  }

  descontoPix(): number {
    return this.form.controls.forma.value === 'pix' ? +(this.carrinho.subtotal() * 0.05).toFixed(2) : 0;
  }

  totalFinal(): number {
    return Math.max(0, +(this.carrinho.subtotal() + this.custoLogistica() - this.descontoPix()).toFixed(2));
  }

  alterarQuantidadeItem(item: ItemCarrinho, delta: number): void {
    this.carrinho.alterarQuantidade(item, item.quantidade + delta);
  }

  buscarCep(): void {
    const controle = this.form.controls.cep;
    if (controle.invalid) {
      controle.markAsTouched();
      return;
    }
    this.buscandoCep.set(true);
    this.avisoCep.set('');
    this.cepService.consultar(somenteNumeros(controle.value)).subscribe({
      next: (endereco) => {
        this.buscandoCep.set(false);
        controle.setValue(formatarCep(controle.value));
        if (!endereco) {
          this.avisoCep.set('CEP não encontrado na base dos Correios.');
          return;
        }
        this.form.patchValue({
          logradouro: endereco.logradouro || this.form.controls.logradouro.value,
          bairro: endereco.bairro || this.form.controls.bairro.value,
          cidade: endereco.localidade,
          uf: endereco.uf,
          complemento: this.form.controls.complemento.value || endereco.complemento,
        });
      },
      error: () => {
        this.buscandoCep.set(false);
        this.avisoCep.set('Não foi possível consultar o CEP agora.');
      },
    });
  }

  confirmar(): void {
    if (this.form.invalid || this.carrinho.itens().length === 0) {
      this.form.markAllAsTouched();
      this.notificacao.erro('Verifique os campos obrigatórios de endereço e faturamento.');
      return;
    }
    const numero = 'AF-' + Math.floor(10000 + Math.random() * 89999);
    const forma = this.form.controls.forma.value;
    const totalPedido = this.totalFinal();

    this.itensFaturados.set([...this.carrinho.itens()]);
    this.subtotalFaturado.set(this.carrinho.subtotal());
    this.freteFaturado.set(this.custoLogistica());
    this.descontoFaturado.set(this.descontoPix());
    this.dataEmissao.set(new Date().toLocaleDateString('pt-BR'));

    this.sessao.registrarPedido({
      numero,
      data: new Date().toISOString().slice(0, 10),
      total: totalPedido,
      itens: this.carrinho.quantidadeTotal(),
      formaPagamento: forma === 'pix' ? 'Pix' : forma === 'cartao' ? 'Cartão de crédito' : 'Boleto',
      situacao: forma === 'cartao' ? 'Pago' : 'Aguardando pagamento',
    });
    this.totalPago.set(totalPedido);
    this.carrinho.limpar();
    this.pedidoConcluido.set(numero);
    this.notificacao.sucesso(`Pedido #${numero} faturado e lote reservado com sucesso!`);
  }

  copiarTexto(texto: string, descricao: string): void {
    if (typeof navigator !== 'undefined' && navigator.clipboard) {
      navigator.clipboard.writeText(texto);
    }
    this.notificacao.sucesso(`${descricao} copiado para a área de transferência.`);
  }

  imprimirPedido(): void {
    if (typeof window !== 'undefined') {
      window.print();
    }
  }

  baixarDocumentoSimulado(documento: string): void {
    this.notificacao.sucesso(`Gerando download de ${documento}...`);
  }

  private ajustarValidacaoCartao(forma: FormaPagamento): void {
    const campos = {
      numeroCartao: [Validators.required, Validators.pattern(/^(\d{4} ?){3}\d{4}$/)],
      nomeCartao: [Validators.required, Validators.minLength(3)],
      validade: [Validators.required, Validators.pattern(/^(0[1-9]|1[0-2])\/\d{2}$/)],
      cvv: [Validators.required, Validators.pattern(/^\d{3,4}$/)],
    };
    for (const [nome, validadores] of Object.entries(campos)) {
      const controle = this.form.get(nome)!;
      controle.setValidators(forma === 'cartao' ? validadores : []);
      controle.updateValueAndValidity();
    }
  }
}
