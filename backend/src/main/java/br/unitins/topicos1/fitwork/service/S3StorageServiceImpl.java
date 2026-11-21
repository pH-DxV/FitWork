package br.unitins.topicos1.fitwork.service;

import java.net.URI;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import br.unitins.topicos1.fitwork.exception.ResourceNotFoundException;
import br.unitins.topicos1.fitwork.exception.ValidationException;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@ApplicationScoped
public class S3StorageServiceImpl implements StorageService {

    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp");

    private static final Map<String, String> EXTENSAO_POR_MIME = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp");

    @ConfigProperty(name = "fitwork.storage.endpoint", defaultValue = "http://localhost:9000")
    String endpoint;

    @ConfigProperty(name = "fitwork.storage.region", defaultValue = "us-east-1")
    String region;

    @ConfigProperty(name = "fitwork.storage.access-key", defaultValue = "fitwork")
    String accessKey;

    @ConfigProperty(name = "fitwork.storage.secret-key", defaultValue = "fitwork123")
    String secretKey;

    @ConfigProperty(name = "fitwork.storage.bucket", defaultValue = "fitwork-arquivos")
    String bucket;

    private volatile S3Client s3Client;
    private volatile boolean bucketVerificado;

    @Override
    public ArquivoArmazenado salvarImagem(String pasta, String nomeOriginal, String contentType, byte[] dados) {
        if (dados == null || dados.length == 0) {
            throw ValidationException.of("arquivo", "O arquivo de imagem não pode estar vazio.");
        }
        if (dados.length > TAMANHO_MAXIMO_BYTES) {
            throw ValidationException.of("arquivo", "A imagem deve ter no máximo 5 MB.");
        }

        String mimeDetectado = detectarMimePorAssinatura(dados);
        String mimeInformado = contentType == null ? "" : contentType.trim().toLowerCase(Locale.ROOT);
        if ("image/jpg".equals(mimeInformado)) {
            mimeInformado = "image/jpeg";
        }

        if (mimeDetectado == null || !TIPOS_PERMITIDOS.contains(mimeDetectado)) {
            throw ValidationException.of("arquivo",
                    "Formato de imagem inválido. Utilize apenas arquivos JPG, PNG ou WEBP.");
        }
        if (!mimeInformado.isEmpty()
                && !"application/octet-stream".equals(mimeInformado)
                && !mimeDetectado.equals(mimeInformado)) {
            throw ValidationException.of("arquivo",
                    "O tipo do arquivo enviado não corresponde ao conteúdo da imagem.");
        }

        String nomeLimpo = sanitizarNomeArquivo(nomeOriginal, EXTENSAO_POR_MIME.get(mimeDetectado));
        String prefixo = (pasta == null || pasta.isBlank()) ? "arquivos" : pasta.replaceAll("^/+|/+$", "");
        String chave = prefixo + "/" + UUID.randomUUID() + "-" + nomeLimpo;

        try {
            S3Client client = obterCliente();
            garantirBucket(client);

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(chave)
                    .contentType(mimeDetectado)
                    .contentLength((long) dados.length)
                    .metadata(Map.of("original-filename", nomeLimpo))
                    .build();

            client.putObject(request, RequestBody.fromBytes(dados));
            return new ArquivoArmazenado(chave, nomeLimpo, mimeDetectado, dados);
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw ValidationException.of("arquivo",
                    "Não foi possível armazenar a imagem no serviço S3 (RustFS). Verifique se o container está em execução.");
        }
    }

    @Override
    public ArquivoArmazenado obter(String chave) {
        if (chave == null || chave.isBlank()) {
            throw new ResourceNotFoundException("Arquivo não encontrado.");
        }
        try {
            S3Client client = obterCliente();
            ResponseBytes<GetObjectResponse> resposta = client.getObjectAsBytes(
                    GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(chave)
                            .build());

            GetObjectResponse meta = resposta.response();
            String contentType = meta.contentType() != null ? meta.contentType() : "application/octet-stream";
            String nomeOriginal = meta.metadata() != null
                    ? meta.metadata().getOrDefault("original-filename", extrairNomeDaChave(chave))
                    : extrairNomeDaChave(chave);

            return new ArquivoArmazenado(chave, nomeOriginal, contentType, resposta.asByteArray());
        } catch (NoSuchKeyException | NoSuchBucketException e) {
            throw new ResourceNotFoundException("Imagem não encontrada no armazenamento S3.");
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                throw new ResourceNotFoundException("Imagem não encontrada no armazenamento S3.");
            }
            throw ValidationException.of("arquivo",
                    "Erro ao recuperar a imagem do serviço S3 (RustFS).");
        } catch (Exception e) {
            throw ValidationException.of("arquivo",
                    "Não foi possível conectar ao serviço S3 (RustFS) para recuperar a imagem.");
        }
    }

    @Override
    public void remover(String chave) {
        if (chave == null || chave.isBlank()) {
            return;
        }
        try {
            obterCliente().deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(chave)
                    .build());
        } catch (Exception ignored) {
            // Não impede a operação de negócio caso o objeto já tenha sido removido ou o S3 esteja offline
        }
    }

    /**
     * Valida os magic bytes da imagem (JPEG, PNG ou WEBP) para garantir integridade do conteúdo.
     */
    static String detectarMimePorAssinatura(byte[] dados) {
        if (dados.length >= 3
                && (dados[0] & 0xFF) == 0xFF
                && (dados[1] & 0xFF) == 0xD8
                && (dados[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (dados.length >= 8
                && (dados[0] & 0xFF) == 0x89
                && dados[1] == 0x50
                && dados[2] == 0x4E
                && dados[3] == 0x47
                && dados[4] == 0x0D
                && dados[5] == 0x0A
                && dados[6] == 0x1A
                && dados[7] == 0x0A) {
            return "image/png";
        }
        if (dados.length >= 12
                && dados[0] == 'R'
                && dados[1] == 'I'
                && dados[2] == 'F'
                && dados[3] == 'F'
                && dados[8] == 'W'
                && dados[9] == 'E'
                && dados[10] == 'B'
                && dados[11] == 'P') {
            return "image/webp";
        }
        return null;
    }

    private static String sanitizarNomeArquivo(String nomeOriginal, String extensaoPadrao) {
        if (nomeOriginal == null || nomeOriginal.isBlank()) {
            return "imagem" + extensaoPadrao;
        }
        String base = nomeOriginal.replace("\\", "/");
        int barra = base.lastIndexOf('/');
        if (barra >= 0) {
            base = base.substring(barra + 1);
        }
        int ponto = base.lastIndexOf('.');
        String semExtensao = ponto > 0 ? base.substring(0, ponto) : base;

        String normalizado = Normalizer.normalize(semExtensao, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9._-]+", "-")
                .replaceAll("^-+|-+$", "");

        if (normalizado.isEmpty()) {
            normalizado = "imagem";
        }
        if (normalizado.length() > 60) {
            normalizado = normalizado.substring(0, 60);
        }
        return normalizado + extensaoPadrao;
    }

    private static String extrairNomeDaChave(String chave) {
        int barra = chave.lastIndexOf('/');
        return barra >= 0 ? chave.substring(barra + 1) : chave;
    }

    private S3Client obterCliente() {
        if (s3Client == null) {
            synchronized (this) {
                if (s3Client == null) {
                    s3Client = S3Client.builder()
                            .endpointOverride(URI.create(endpoint))
                            .region(Region.of(region))
                            .credentialsProvider(StaticCredentialsProvider.create(
                                    AwsBasicCredentials.create(accessKey, secretKey)))
                            .forcePathStyle(true)
                            .httpClientBuilder(UrlConnectionHttpClient.builder())
                            .build();
                }
            }
        }
        return s3Client;
    }

    private void garantirBucket(S3Client client) {
        if (bucketVerificado) {
            return;
        }
        synchronized (this) {
            if (bucketVerificado) {
                return;
            }
            try {
                client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
                bucketVerificado = true;
            } catch (NoSuchBucketException e) {
                client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
                bucketVerificado = true;
            } catch (S3Exception e) {
                if (e.statusCode() == 404) {
                    client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
                    bucketVerificado = true;
                } else {
                    throw e;
                }
            }
        }
    }

    @PreDestroy
    void fechar() {
        if (s3Client != null) {
            s3Client.close();
        }
    }
}
