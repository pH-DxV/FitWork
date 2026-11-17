import { FormControl } from '@angular/forms';
import { describe, expect, it } from 'vitest';
import { Validadores } from './validadores';

describe('Validadores', () => {
  it('deve rejeitar texto contendo apenas espaços ou 1 caractere útil cercado por espaços', () => {
    expect(Validadores.naoSomenteEspacos(new FormControl('   '))).toEqual({ somenteEspacos: true });
    expect(Validadores.naoSomenteEspacos(new FormControl(' a '))).toEqual({
      minlength: { requiredLength: 2, actualLength: 1 },
    });
    expect(Validadores.naoSomenteEspacos(new FormControl(' ab '))).toBeNull();
  });

  it('deve validar formato de preço com até 8 inteiros e 2 decimais', () => {
    expect(Validadores.preco(new FormControl(199.9))).toBeNull();
    expect(Validadores.preco(new FormControl('199.99'))).toBeNull();
    expect(Validadores.preco(new FormControl('199.999'))).toEqual({ precoFormato: true });
    expect(Validadores.preco(new FormControl('123456789.00'))).toEqual({ precoFormato: true });
  });

  it('deve validar dígitos verificadores de CPF e CNPJ', () => {
    expect(Validadores.cpf(new FormControl('529.982.247-25'))).toBeNull();
    expect(Validadores.cpf(new FormControl('111.111.111-11'))).toEqual({ cpfInvalido: true });

    expect(Validadores.cnpj(new FormControl('11.222.333/0001-81'))).toBeNull();
    expect(Validadores.cnpj(new FormControl('11.222.333/0001-00'))).toEqual({ cnpjInvalido: true });
  });
});
