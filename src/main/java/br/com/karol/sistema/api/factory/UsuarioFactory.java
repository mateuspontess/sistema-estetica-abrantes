package br.com.karol.sistema.api.factory;

import org.springframework.stereotype.Component;

import br.com.karol.sistema.domain.Usuario;
import br.com.karol.sistema.domain.enums.UserRole;
import br.com.karol.sistema.domain.valueobjects.Login;
import br.com.karol.sistema.domain.valueobjects.Senha;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class UsuarioFactory {

    private final LoginFactory loginFactory;
    private final SenhaFactory senhaFactory;

    public Usuario criarUsuarioUser(String nome, String loginString, String senhaString) {
        return this.criarUsuario(nome, loginString, senhaString, UserRole.USER);
    }

    public Usuario criarUsuarioAdmin(String nome, String loginString, String senhaString) {
        return this.criarUsuario(nome, loginString, senhaString, UserRole.ADMIN);
    }

    public Usuario criarUsuarioClient(String nome, String loginString, String senhaString) {
        return this.criarUsuario(nome, loginString, senhaString, UserRole.CLIENT);
    }

    private Usuario criarUsuario(String nome, String loginString, String senhaString, UserRole role) {
        Login login = this.loginFactory.criarLogin(loginString);
        Senha senha = this.senhaFactory.criarSenha(senhaString);
        return new Usuario(nome, login, senha, role);
    }
}