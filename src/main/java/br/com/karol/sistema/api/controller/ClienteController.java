package br.com.karol.sistema.api.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.karol.sistema.api.dto.cliente.AtualizarClienteDTO;
import br.com.karol.sistema.api.dto.cliente.CriarClienteDTO;
import br.com.karol.sistema.api.dto.cliente.CriarUsuarioClienteDTO;
import br.com.karol.sistema.api.dto.cliente.DadosClienteDTO;
import br.com.karol.sistema.api.dto.cliente.DadosCompletosClienteDTO;
import br.com.karol.sistema.api.dto.email.EmailDTO;
import br.com.karol.sistema.api.dto.endereco.EnderecoDTO;
import br.com.karol.sistema.business.service.ClienteService;
import br.com.karol.sistema.business.service.EmailSendService;
import br.com.karol.sistema.domain.Cliente;
import br.com.karol.sistema.domain.Usuario;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RequestMapping("/clientes")
@RestController
@AllArgsConstructor
public class ClienteController {

    private final EmailSendService emailSender;
    private final ClienteService service;

    /*
     * Rotas para clientes não atenticados. São clientes que não possuem um
     * usuário e são cadastrados pelos funcionários da clínica em atendimento
     * direto com o cliente
     */

    @PostMapping("/email")
    public ResponseEntity<DadosCompletosClienteDTO> emailVerification(@RequestBody @Valid EmailDTO dto) {
        emailSender.sendEmailVerification(dto.getEmail());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/usuario")
    public ResponseEntity<DadosCompletosClienteDTO> criarClienteUsuario(
            @RequestBody @Valid CriarUsuarioClienteDTO dados) {
        return ResponseEntity.ok(service.salvarClienteComUsuario(dados));
    }

    @PostMapping
    public ResponseEntity<DadosCompletosClienteDTO> criarCliente(@RequestBody @Valid CriarClienteDTO cliente) {
        DadosCompletosClienteDTO clienteSalvo = service.salvarCliente(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteSalvo);
    }

    @GetMapping
    public ResponseEntity<Page<DadosClienteDTO>> listarClientes(
            @RequestParam(required = false) String nome,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(service.listarTodosClientes(nome, pageable));
    }

    @GetMapping("/{clienteId}")
    public ResponseEntity<DadosCompletosClienteDTO> buscarCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(service.buscarClientePorId(clienteId));
    }

    @PutMapping("/{clienteId}")
    public ResponseEntity<DadosCompletosClienteDTO> atualizarCliente(@PathVariable Long clienteId,
            @RequestBody AtualizarClienteDTO dados) {
        return ResponseEntity.ok(service.editarContatoCliente(clienteId, dados));
    }

    @PutMapping("/{clienteId}/endereco")
    public ResponseEntity<DadosCompletosClienteDTO> atualizarEnderecoCliente(
            @PathVariable Long clienteId,
            @RequestBody @Valid EnderecoDTO dadosEndereco) {
        return ResponseEntity.ok(
                service.editarEnderecoCliente(clienteId, dadosEndereco));
    }

    @DeleteMapping("/{clienteId}")
    public ResponseEntity<Void> deletarCliente(@PathVariable Long clienteId) {
        service.removerCliente(clienteId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // -- Rotas para clientes autenticados

    @GetMapping("/me")
    public ResponseEntity<DadosCompletosClienteDTO> buscarDadosClienteMe(@AuthenticationPrincipal Usuario usuario) {
        Cliente cliente = service.getClienteByUsuarioId(usuario.getId());
        return ResponseEntity.ok(new DadosCompletosClienteDTO(cliente));
    }

    @PutMapping("/me")
    public ResponseEntity<DadosCompletosClienteDTO> atualizarClienteMe(
            @AuthenticationPrincipal Usuario usuario,
            @RequestBody AtualizarClienteDTO dados) {
        Cliente cliente = service.getClienteByUsuarioId(usuario.getId());
        return ResponseEntity.ok(
                service.editarContatoClienteAtual(cliente, dados));
    }

    @PutMapping("/me/endereco")
    public ResponseEntity<DadosCompletosClienteDTO> atualizarEnderecoClienteMe(
            @AuthenticationPrincipal Usuario usuario,
            @RequestBody @Valid EnderecoDTO dadosEndereco) {
        Cliente cliente = service.getClienteByUsuarioId(usuario.getId());
        return ResponseEntity.ok(
                service.editarEnderecoClienteAtual(cliente, dadosEndereco));
    }
}