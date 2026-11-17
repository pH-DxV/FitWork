package br.unitins.topicos1.fitwork.service;

import br.unitins.topicos1.fitwork.dto.MunicipioResponseDTO;
import br.unitins.topicos1.fitwork.dto.PageResponse;
import br.unitins.topicos1.fitwork.model.Municipio;

public interface MunicipioService {

    PageResponse<MunicipioResponseDTO> findAll(int page, int pageSize);

    PageResponse<MunicipioResponseDTO> findByNome(String nome, int page, int pageSize);

    MunicipioResponseDTO findById(long id);

    /**
     * Localiza o município pelo código IBGE (ou pelo nome + UF). Se ainda não
     * existir no banco, cadastra usando os dados vindos do ViaCEP.
     */
    Municipio buscarOuCriar(String nome, String uf, String codigoIbge);
}
