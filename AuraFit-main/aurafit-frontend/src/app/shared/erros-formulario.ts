import { HttpErrorResponse } from '@angular/common/http';
import { AbstractControl, FormGroup } from '@angular/forms';
import { ProblemaApi } from '../models/problema-api.model';

/**
 * Converte os erros do formulário em uma mensagem amigável.
 * O erro "api" é preenchido com a mensagem que veio do back-end.
 */
export function mensagemErro(controle: AbstractControl | null): string {
  const erros = controle?.errors;
  if (!erros) return '';

  if (erros['api']) return erros['api'] as string;
  if (erros['required'] || erros['somenteEspacos']) return 'Campo obrigatório.';
  if (erros['minlength']) return `Use pelo menos ${erros['minlength'].requiredLength} caracteres.`;
  if (erros['maxlength']) return `Use no máximo ${erros['maxlength'].requiredLength} caracteres.`;
  if (erros['email']) return 'E-mail inválido.';
  if (erros['min']) return `O valor mínimo é ${erros['min'].min}.`;
  if (erros['max']) return `O valor máximo é ${erros['max'].max}.`;
  if (erros['pattern']) return 'Formato inválido.';
  if (erros['cpfFormato']) return 'O CPF deve ter 11 números.';
  if (erros['cpfInvalido']) return 'CPF inválido.';
  if (erros['cnpjFormato']) return 'O CNPJ deve ter 14 números.';
  if (erros['cnpjInvalido']) return 'CNPJ inválido.';
  if (erros['telefone']) return 'Informe DDD e número (10 ou 11 dígitos).';
  if (erros['cep']) return 'CEP inválido. Use 8 números, como 77001-002.';
  if (erros['data']) return 'Data inválida.';
  if (erros['dataFutura']) return 'A data deve ser anterior a hoje.';
  if (erros['idadeMinima']) return `É preciso ter pelo menos ${erros['idadeMinima'].requerida} anos.`;
  return 'Valor inválido.';
}

/**
 * Aplica no formulário os erros de campo devolvidos pela API (RFC 7807).
 * O back-end envia o caminho do campo (ex.: "endereco.cep", "telefones.0.numero"),
 * que é o mesmo formato aceito por form.get(...).
 *
 * Retorna a mensagem geral a exibir (ou '' quando todos os erros foram ligados a campos).
 */
export function aplicarErrosDaApi(form: FormGroup, erro: HttpErrorResponse): string {
  const problema = erro.error as ProblemaApi | null;

  if (erro.status === 0) {
    return 'Não foi possível conectar ao servidor. O back-end está rodando?';
  }

  const naoAssociados: string[] = [];
  for (const erroCampo of problema?.errors ?? []) {
    const campo = form.get(erroCampo.field);
    if (campo && campo.enabled) {
      campo.setErrors({ ...(campo.errors ?? {}), api: erroCampo.message });
      campo.markAsTouched();
    } else {
      naoAssociados.push(erroCampo.message);
    }
  }

  if (problema?.errors?.length && naoAssociados.length === 0) return '';
  if (naoAssociados.length) return naoAssociados.join(' ');
  return problema?.detail ?? problema?.title ?? 'Não foi possível concluir a operação.';
}

/** Mensagem de erro para operações fora de formulário (ex.: exclusão). */
export function mensagemDaApi(erro: HttpErrorResponse, padrao: string): string {
  if (erro.status === 0) return 'Não foi possível conectar ao servidor. O back-end está rodando?';
  const problema = erro.error as ProblemaApi | null;
  return problema?.errors?.[0]?.message ?? problema?.detail ?? padrao;
}
