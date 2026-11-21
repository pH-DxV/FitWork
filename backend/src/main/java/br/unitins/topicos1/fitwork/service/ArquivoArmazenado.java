package br.unitins.topicos1.fitwork.service;

/**
 * Representa um arquivo armazenado ou recuperado do Object Storage S3 (RustFS).
 */
public record ArquivoArmazenado(
    String chave,
    String nomeArquivo,
    String contentType,
    byte[] conteudo
) {
}
