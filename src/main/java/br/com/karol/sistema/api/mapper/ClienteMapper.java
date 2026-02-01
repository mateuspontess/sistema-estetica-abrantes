package br.com.karol.sistema.api.mapper;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import br.com.karol.sistema.api.dto.cliente.CriarClienteDTO;
import br.com.karol.sistema.api.dto.cliente.CriarUsuarioClienteDTO;
import br.com.karol.sistema.api.dto.cliente.DadosClienteDTO;
import br.com.karol.sistema.api.dto.cliente.DadosCompletosClienteDTO;
import br.com.karol.sistema.api.factory.ClientFactory;
import br.com.karol.sistema.domain.Cliente;
import br.com.karol.sistema.domain.Usuario;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class ClienteMapper {

    private final ClientFactory clientFactory;
    private final EnderecoMapper enderecoMapper;

    public Cliente toCliente(CriarClienteDTO dados) {
        return clientFactory.criarCliente(
                dados.getNome(),
                dados.getCpf(),
                dados.getTelefone(),
                dados.getEmail(),
                enderecoMapper.toEndereco(dados.getEndereco()));
    }

    public Cliente toClienteComUsuario(CriarUsuarioClienteDTO dados, Usuario usuario) {
        return clientFactory.criarClienteComUsuario(
                dados.getNome(),
                dados.getCpf(),
                dados.getTelefone(),
                dados.getEmail(),
                enderecoMapper.toEndereco(dados.getEndereco()),
                usuario);
    }

    public DadosCompletosClienteDTO toDadosCompletosClienteDTO(Cliente cliente) {
        return new DadosCompletosClienteDTO(cliente);
    }

    public DadosClienteDTO toDadosClienteDTO(Cliente cliente) {
        return new DadosClienteDTO(cliente);
    }

    public Page<DadosClienteDTO> toPageDadosClienteDTO(Page<Cliente> clienteList) {
        return clienteList.map(c -> this.toDadosClienteDTO(c));
    }
}