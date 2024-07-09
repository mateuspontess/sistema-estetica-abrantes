package br.com.karol.sistema.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.karol.sistema.api.dto.EnderecoDTO;
import br.com.karol.sistema.api.dto.cliente.AtualizarClienteDTO;
import br.com.karol.sistema.api.dto.cliente.CriarClienteDTO;
import br.com.karol.sistema.api.dto.cliente.DadosClienteDTO;
import br.com.karol.sistema.api.dto.cliente.DadosCompletosClienteDTO;
import br.com.karol.sistema.business.service.ClienteService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;


@RequestMapping("/clientes")
@RestController
@AllArgsConstructor
public class ClienteController {

    private final ClienteService service;


    @PostMapping
    public ResponseEntity<DadosCompletosClienteDTO> criarCliente(@RequestBody @Valid CriarClienteDTO cliente) {
        DadosCompletosClienteDTO clienteSalvo = service.salvarCliente(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteSalvo);
    }

    @GetMapping
    public ResponseEntity<List<DadosClienteDTO>> listarClientes() {
        return ResponseEntity.status(HttpStatus.OK).body(service.listarTodosClientes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DadosCompletosClienteDTO> buscarCliente(@PathVariable String id) {
        DadosCompletosClienteDTO dto = service.buscarClientePorId(id);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DadosCompletosClienteDTO> atualizarCliente(@PathVariable String id, @RequestBody AtualizarClienteDTO cliente) {
        return ResponseEntity.ok(service.editarContatoCliente(id, cliente));
    }

    @PutMapping("/{id}/endereco")
    public ResponseEntity<DadosCompletosClienteDTO> atualizarEnderecoCliente(@PathVariable String id, @RequestBody @Valid EnderecoDTO cliente) {
        return ResponseEntity.ok(service.editarEnderecoCliente(id, cliente));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarCliente(@PathVariable String id) {
        service.removerCliente(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}