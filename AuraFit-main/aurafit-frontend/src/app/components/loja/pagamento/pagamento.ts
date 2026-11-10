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
import { CarrinhoService } from '../../../services/carrinho.service';
import { CepService } from '../../../services/cep.service';
import { SessaoService } from '../../../services/sessao.service';
import { mensagemErro } from '../../../shared/erros-formulario';
import { formatarCep, somenteNumeros } from '../../../shared/formatos';
import { Validadores } from '../../../shared/validadores';

type FormaPagamento = 'pix' | 'cartao' | 'boleto';

/** Protótipo da tela de pagamento (checkout). Não processa pagamentos reais. */
@Component({
  selector: 'app-pagamento',
  imports: [
    CurrencyPipe, ReactiveFormsModule, RouterLink, MatFormFieldModule, MatInputModule, MatSelectModule,
    MatRadioModule, MatButtonModule, MatIconModule,
  ],
  templateUrl: './pagamento.html',
  styleUrl: './pagamento.css',
})
export class Pagamento implements OnInit {
  private fb = inject(FormBuilder);
  private cepService = inject(CepService);
  private sessao = inject(SessaoService);
  private router = inject(Router);
  carrinho = inject(CarrinhoService);

  buscandoCep = signal(false);
  avisoCep = signal('');
  pedidoConcluido = signal<string | null>(null);
  totalPago = signal(0);

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
    }
    this.form.controls.forma.valueChanges.subscribe((forma) => this.ajustarValidacaoCartao(forma));
  }

  parcelas(): number[] {
    return [1, 2, 3, 4, 5, 6];
  }

  buscarCep(): void {
    const controle = this.form.controls.cep;
    if (controle.invalid) return;
    this.buscandoCep.set(true);
    this.avisoCep.set('');
    this.cepService.consultar(somenteNumeros(controle.value)).subscribe({
      next: (endereco) => {
        this.buscandoCep.set(false);
        controle.setValue(formatarCep(controle.value));
        if (!endereco) {
          this.avisoCep.set('CEP não encontrado.');
          return;
        }
        this.form.patchValue({
          logradouro: endereco.logradouro,
          bairro: endereco.bairro,
          cidade: endereco.localidade,
          uf: endereco.uf,
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
      return;
    }
    const numero = 'AF-' + Math.floor(10000 + Math.random() * 89999);
    const forma = this.form.controls.forma.value;
    this.sessao.registrarPedido({
      numero,
      data: new Date().toISOString().slice(0, 10),
      total: this.carrinho.total(),
      itens: this.carrinho.quantidadeTotal(),
      formaPagamento: forma === 'pix' ? 'Pix' : forma === 'cartao' ? 'Cartão de crédito' : 'Boleto',
      situacao: 'Aguardando pagamento',
    });
    this.totalPago.set(this.carrinho.total());
    this.carrinho.limpar();
    this.pedidoConcluido.set(numero);
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
