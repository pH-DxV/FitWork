package br.unitins.topicos1.fitwork.service;

/**
 * Serviço genérico de armazenamento de objetos compatível com a API S3 (RustFS).
 * Pode ser utilizado para imagens de produtos, fotos de usuários e demais arquivos do sistema.
 */
public interface StorageService {

    long TAMANHO_MAXIMO_BYTES = 5L * 1024 * 1024; // 5 MB

    /**
     * Valida o tipo e o tamanho da imagem e a armazena no bucket S3 sob a pasta informada.
     */
    ArquivoArmazenado salvarImagem(String pasta, String nomeOriginal, String contentType, byte[] dados);

    /**
     * Recupera os bytes e metadados de um arquivo armazenado pela sua chave S3.
     */
    ArquivoArmazenado obter(String chave);

    /**
     * Remove um arquivo armazenado pela sua chave S3 (ignora silenciosamente se a chave for nula/vazia).
     */
    void remover(String chave);
}
