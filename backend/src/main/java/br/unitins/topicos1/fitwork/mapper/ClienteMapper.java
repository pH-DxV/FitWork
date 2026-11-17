package br.unitins.topicos1.fitwork.mapper;

import br.unitins.topicos1.fitwork.dto.ClienteResponseDTO;
import br.unitins.topicos1.fitwork.model.Cliente;
import br.unitins.topicos1.fitwork.model.Pessoa;

public final class ClienteMapper {

    private ClienteMapper() {
    }

    public static ClienteResponseDTO toResponse(Cliente cliente) {
        Pessoa pessoa = cliente.getPessoa();
        return new ClienteResponseDTO(
                cliente.getId(),
                pessoa.getNome(),
                pessoa.getEmail(),
                pessoa.getCpf(),
                pessoa.getDataNascimento(),
                pessoa.getGenero(),
                pessoa.getTelefones().stream().map(TelefoneMapper::toResponse).toList(),
                EnderecoMapper.toResponse(pessoa.getEndereco()),
                cliente.isAceitaNovidades(),
                cliente.isAtivo(),
                cliente.getDataCadastro());
    }
}
