import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { somenteNumeros } from './formatos';

/** Validadores reutilizáveis do front-end (espelham as regras do back-end). */
export class Validadores {
  /** Impede valores formados apenas por espaços. */
  static naoSomenteEspacos(control: AbstractControl): ValidationErrors | null {
    const valor = control.value as string | null;
    return valor && valor.trim().length === 0 ? { somenteEspacos: true } : null;
  }

  static cpf(control: AbstractControl): ValidationErrors | null {
    const valor = control.value as string | null;
    if (!valor) return null;

    const cpf = somenteNumeros(valor);
    if (/[^\d.\-\s]/.test(valor) || cpf.length !== 11) return { cpfFormato: true };
    if (/^(\d)\1{10}$/.test(cpf)) return { cpfInvalido: true };

    for (let posicao = 9; posicao <= 10; posicao++) {
      let soma = 0;
      for (let i = 0; i < posicao; i++) soma += Number(cpf[i]) * (posicao + 1 - i);
      let digito = (soma * 10) % 11;
      if (digito === 10) digito = 0;
      if (digito !== Number(cpf[posicao])) return { cpfInvalido: true };
    }
    return null;
  }

  static cnpj(control: AbstractControl): ValidationErrors | null {
    const valor = control.value as string | null;
    if (!valor) return null;

    const cnpj = somenteNumeros(valor);
    if (/[^\d.\-/\s]/.test(valor) || cnpj.length !== 14) return { cnpjFormato: true };
    if (/^(\d)\1{13}$/.test(cnpj)) return { cnpjInvalido: true };

    const pesos = [
      [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2],
      [6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2],
    ];
    for (let etapa = 0; etapa < 2; etapa++) {
      const tamanho = 12 + etapa;
      let soma = 0;
      for (let i = 0; i < tamanho; i++) soma += Number(cnpj[i]) * pesos[etapa][i];
      const resto = soma % 11;
      const digito = resto < 2 ? 0 : 11 - resto;
      if (digito !== Number(cnpj[tamanho])) return { cnpjInvalido: true };
    }
    return null;
  }

  /** Telefone completo: DDD + número (10 ou 11 dígitos). */
  static telefone(control: AbstractControl): ValidationErrors | null {
    const valor = control.value as string | null;
    if (!valor) return null;
    const tamanho = somenteNumeros(valor).length;
    return tamanho === 10 || tamanho === 11 ? null : { telefone: true };
  }

  static cep(control: AbstractControl): ValidationErrors | null {
    const valor = control.value as string | null;
    if (!valor) return null;
    return /^\d{5}-?\d{3}$/.test(valor.trim()) ? null : { cep: true };
  }

  /** Data (yyyy-MM-dd) anterior a hoje e idade mínima opcional. */
  static dataNascimento(idadeMinima = 0): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const valor = control.value as string | null;
      if (!valor) return null;

      const [ano, mes, dia] = valor.split('-').map(Number);
      const nascimento = new Date(ano, mes - 1, dia);
      const hoje = new Date();
      hoje.setHours(0, 0, 0, 0);

      if (isNaN(nascimento.getTime())) return { data: true };
      if (nascimento >= hoje) return { dataFutura: true };

      let idade = hoje.getFullYear() - ano;
      const jaFezAniversario =
        hoje.getMonth() > mes - 1 || (hoje.getMonth() === mes - 1 && hoje.getDate() >= dia);
      if (!jaFezAniversario) idade--;

      return idade < idadeMinima ? { idadeMinima: { requerida: idadeMinima } } : null;
    };
  }
}
