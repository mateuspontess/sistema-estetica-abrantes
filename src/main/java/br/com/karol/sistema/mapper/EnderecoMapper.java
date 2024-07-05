package br.com.karol.sistema.mapper;

import org.springframework.stereotype.Component;

import br.com.karol.sistema.domain.Endereco;
import br.com.karol.sistema.dto.EnderecoDTO;

@Component
public class EnderecoMapper {

    public EnderecoDTO toEnderecoDTO(Endereco endereco) {
        return new EnderecoDTO(
            endereco.getRua(),
            endereco.getNumero(),
            endereco.getCidade(),
            endereco.getBairro(),
            endereco.getEstado()
        );
    }

    public Endereco toEndereco(EnderecoDTO enderecoDTO) {
        Endereco endereco = new Endereco();
        endereco.setRua(enderecoDTO.getRua());
        endereco.setNumero(enderecoDTO.getNumero());
        endereco.setCidade(enderecoDTO.getCidade());
        endereco.setBairro(enderecoDTO.getBairro());
        endereco.setEstado(enderecoDTO.getEstado());
        return endereco;
    }
}