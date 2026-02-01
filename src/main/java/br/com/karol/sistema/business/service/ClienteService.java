package br.com.karol.sistema.business.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.karol.sistema.api.dto.cliente.AtualizarClienteDTO;
import br.com.karol.sistema.api.dto.cliente.CriarClienteDTO;
import br.com.karol.sistema.api.dto.cliente.CriarUsuarioClienteDTO;
import br.com.karol.sistema.api.dto.cliente.DadosClienteDTO;
import br.com.karol.sistema.api.dto.cliente.DadosCompletosClienteDTO;
import br.com.karol.sistema.api.dto.endereco.EnderecoDTO;
import br.com.karol.sistema.api.dto.usuario.CriarUsuarioDTO;
import br.com.karol.sistema.api.factory.EmailFactory;
import br.com.karol.sistema.api.factory.TelefoneFactory;
import br.com.karol.sistema.api.mapper.ClienteMapper;
import br.com.karol.sistema.api.mapper.EnderecoMapper;
import br.com.karol.sistema.domain.Cliente;
import br.com.karol.sistema.domain.Endereco;
import br.com.karol.sistema.domain.Usuario;
import br.com.karol.sistema.domain.valueobjects.Email;
import br.com.karol.sistema.domain.valueobjects.Telefone;
import br.com.karol.sistema.infra.exceptions.EntityNotFoundException;
import br.com.karol.sistema.infra.repository.ClienteRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ClienteService {

    private static final String NOT_FOUND_MESSAGE = "Cliente não encontrado";

    private final ClienteRepository repository;
    private final ClienteMapper clienteMapper;
    private final TelefoneFactory telefoneMapper;
    private final EmailFactory emailMapper;
    private final EnderecoMapper enderecoMapper;
    private final UsuarioService usuarioService;
    private final VerificationService verificationService;

    @Transactional
    public DadosCompletosClienteDTO salvarCliente(CriarClienteDTO dadosCriacaoCliente) {
        Cliente cliente = clienteMapper.toCliente(dadosCriacaoCliente);
        Cliente savedCliente = repository.save(cliente);
        return clienteMapper.toDadosCompletosClienteDTO(savedCliente);
    }

    @Transactional
    public DadosCompletosClienteDTO salvarClienteComUsuario(CriarUsuarioClienteDTO dados) {
        verificationService.validateCode(dados.getEmail(), dados.getVerificationCode());

        Usuario usuario = this.usuarioService.salvarUsuarioCliente(
                new CriarUsuarioDTO(
                        dados.getNome(),
                        dados.getLogin(),
                        dados.getSenha()));

        Cliente cliente = clienteMapper.toClienteComUsuario(dados, usuario);

        Cliente savedCliente = repository.save(cliente);
        return clienteMapper.toDadosCompletosClienteDTO(savedCliente);
    }

    public Page<DadosClienteDTO> listarTodosClientes(String nome, Pageable pageable) {
        return clienteMapper.toPageDadosClienteDTO(
                repository.findAllByParams(nome, pageable));
    }

    public DadosCompletosClienteDTO buscarClientePorId(Long clienteId) {
        Cliente cliente = this.buscarPorId(clienteId);
        return clienteMapper.toDadosCompletosClienteDTO(cliente);
    }

    @Transactional
    public DadosCompletosClienteDTO editarContatoCliente(Long clienteId, AtualizarClienteDTO dados) {
        Cliente alvo = this.buscarPorId(clienteId);

        String novoNome = dados.getNome();
        Telefone novoTelefone = telefoneMapper.toTelefoneOrNull(dados.getTelefone());
        Email novoEmail = emailMapper.toEmailOrNull(dados.getEmail());
        alvo.atualizarDados(
                novoNome,
                novoTelefone,
                novoEmail);

        return clienteMapper.toDadosCompletosClienteDTO(repository.save(alvo));
    }

    @Transactional
    public DadosCompletosClienteDTO editarContatoClienteAtual(Cliente cliente, AtualizarClienteDTO dados) {
        String novoNome = dados.getNome();
        Telefone novoTelefone = telefoneMapper.toTelefoneOrNull(dados.getTelefone());
        Email novoEmail = emailMapper.toEmailOrNull(dados.getEmail());

        cliente.atualizarDados(
                novoNome,
                novoTelefone,
                novoEmail);

        return clienteMapper.toDadosCompletosClienteDTO(repository.save(cliente));
    }

    @Transactional
    public DadosCompletosClienteDTO editarEnderecoCliente(Long clienteId, EnderecoDTO dadosAtualizacao) {
        Cliente alvo = this.buscarPorId(clienteId);
        Endereco novoEndereco = enderecoMapper.toEndereco(dadosAtualizacao);
        alvo.atualizarEndereco(novoEndereco);

        return clienteMapper.toDadosCompletosClienteDTO(repository.save(alvo));
    }

    @Transactional
    public DadosCompletosClienteDTO editarEnderecoClienteAtual(Cliente cliente, EnderecoDTO dadosAtualizacao) {
        Endereco novoEndereco = enderecoMapper.toEndereco(dadosAtualizacao);
        cliente.atualizarEndereco(novoEndereco);

        return clienteMapper.toDadosCompletosClienteDTO(repository.save(cliente));
    }

    @Transactional
    public void removerCliente(Long clienteId) {
        if (!repository.existsById(clienteId))
            throw new EntityNotFoundException(NOT_FOUND_MESSAGE);
        repository.deleteById(clienteId);
    }

    public Cliente getClienteByUsuarioId(Long usuarioId) {
        return repository
                .findByUsuarioId(usuarioId).orElseThrow(EntityNotFoundException::new);
    }

    public Cliente buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE));
    }
}