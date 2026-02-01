package br.com.karol.sistema.api.dto.cliente;

import br.com.karol.sistema.api.dto.endereco.EnderecoDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CriarUsuarioClienteDTO {

    @NotBlank
    private String nome;

    @NotBlank
    @Size(min = 3, max = 20)
    private String login;

    @NotBlank
    @Size(min = 8, max = 20)
    private String senha;

    @NotBlank
    @Size(min = 11, max = 14)
    private String cpf;

    @NotBlank
    @Size(min = 11, max = 15)
    private String telefone;

    @NotNull
    private EnderecoDTO endereco;

    @NotBlank
    private String email;

    @NotBlank
    private String verificationCode;
}