/** Funções de máscara/formatação reaproveitadas nas telas. */

export function somenteNumeros(texto: string | null | undefined): string {
  return (texto ?? '').replace(/\D/g, '');
}

export function formatarCpf(cpf: string | null | undefined): string {
  return somenteNumeros(cpf).replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
}

export function formatarCnpj(cnpj: string | null | undefined): string {
  return somenteNumeros(cnpj).replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, '$1.$2.$3/$4-$5');
}

export function formatarCep(cep: string | null | undefined): string {
  return somenteNumeros(cep).replace(/(\d{5})(\d{3})/, '$1-$2');
}

export function formatarTelefone(telefone: string | null | undefined): string {
  return somenteNumeros(telefone).replace(/(\d{2})(\d{4,5})(\d{4})/, '($1) $2-$3');
}

/** Número de telefone sem DDD (8 ou 9 dígitos). */
export function formatarNumeroTelefone(numero: string | null | undefined): string {
  return somenteNumeros(numero).replace(/(\d{4,5})(\d{4})/, '$1-$2');
}
