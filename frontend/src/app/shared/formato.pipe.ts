import { Pipe, PipeTransform } from '@angular/core';
import { formatarCep, formatarCnpj, formatarCpf, formatarTelefone } from './formatos';

export type TipoFormato = 'cpf' | 'cnpj' | 'cep' | 'telefone';

/** Uso: {{ cliente.cpf | formato: 'cpf' }} */
@Pipe({ name: 'formato' })
export class FormatoPipe implements PipeTransform {
  transform(valor: string | null | undefined, tipo: TipoFormato): string {
    switch (tipo) {
      case 'cpf':
        return formatarCpf(valor);
      case 'cnpj':
        return formatarCnpj(valor);
      case 'cep':
        return formatarCep(valor);
      case 'telefone':
        return formatarTelefone(valor);
    }
  }
}
