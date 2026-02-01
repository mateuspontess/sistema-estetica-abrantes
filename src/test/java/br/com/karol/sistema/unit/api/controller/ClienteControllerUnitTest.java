package br.com.karol.sistema.unit.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import br.com.karol.sistema.api.controller.ClienteController;
import br.com.karol.sistema.api.dto.cliente.AtualizarClienteDTO;
import br.com.karol.sistema.api.dto.cliente.CriarClienteDTO;
import br.com.karol.sistema.api.dto.cliente.CriarUsuarioClienteDTO;
import br.com.karol.sistema.api.dto.cliente.DadosClienteDTO;
import br.com.karol.sistema.api.dto.cliente.DadosCompletosClienteDTO;
import br.com.karol.sistema.api.dto.email.EmailDTO;
import br.com.karol.sistema.api.dto.endereco.EnderecoDTO;
import br.com.karol.sistema.builder.ClienteTestFactory;
import br.com.karol.sistema.builder.UsuarioTestFactory;
import br.com.karol.sistema.business.service.ClienteService;
import br.com.karol.sistema.business.service.EmailSendService;
import br.com.karol.sistema.business.service.TokenService;
import br.com.karol.sistema.config.ContextualizeUsuarioTypeWithRoles;
import br.com.karol.sistema.constants.TestConstants;
import br.com.karol.sistema.domain.Cliente;
import br.com.karol.sistema.infra.repository.UsuarioRepository;
import br.com.karol.sistema.infra.security.SecurityConfig;
import br.com.karol.sistema.utils.ControllerTestUtils;

@AutoConfigureJsonTesters
@Import(SecurityConfig.class)
@WebMvcTest(ClienteController.class)
public class ClienteControllerUnitTest {

    private static final String BASE_URL = "/clientes";
    private static final String ME_ROUTE = BASE_URL + "/me";

    private static final Cliente CLIENTE_DEFAULT = ClienteTestFactory.getCliente();

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ClienteService service;
    @MockBean
    private EmailSendService emailSendService;

    @Autowired
    private JacksonTester<EmailDTO> emailDTOJson;
    @Autowired
    private JacksonTester<CriarClienteDTO> criarClienteDTO;
    @Autowired
    private JacksonTester<CriarUsuarioClienteDTO> criarUsuarioClienteDTOJson;
    @Autowired
    private JacksonTester<AtualizarClienteDTO> atualizarClienteDTOJson;
    @Autowired
    private JacksonTester<EnderecoDTO> enderecoDTOJson;

    // Dependências SecurityFilter
    @MockBean
    private TokenService tokenService;
    @MockBean
    private UsuarioRepository usuarioRepository;

    @BeforeAll
    static void setup() {
        ReflectionTestUtils.setField(CLIENTE_DEFAULT, "usuario", UsuarioTestFactory.getUsuarioAdmin());
    }

    @Test
    void testEmailVerification() throws IOException, Exception {
        // arrange
        EmailDTO requestBody = new EmailDTO("random@email.com");

        // act
        ControllerTestUtils.postRequest(
                mvc,
                BASE_URL + "/email",
                emailDTOJson.write(requestBody).getJson())

                // assert
                .andExpect(status().isNoContent());

        verify(emailSendService).sendEmailVerification(anyString());
    }

    @Test
    void testEmailVerification_comBodyInvalido() throws IOException, Exception {
        // arrange
        EmailDTO requestBodyBlank = new EmailDTO("");

        // act
        ControllerTestUtils.postRequest(
                mvc,
                BASE_URL + "/email",
                emailDTOJson.write(requestBodyBlank).getJson())

                // assert
                .andExpect(status().isBadRequest());

        verifyNoInteractions(emailSendService);
    }

    @Test
    void testCriarClienteComUsuario() throws Exception {
        // arrange
        var requestBody = new CriarUsuarioClienteDTO(
                CLIENTE_DEFAULT.getNome(),
                CLIENTE_DEFAULT.getUsuario().getLogin(),
                CLIENTE_DEFAULT.getUsuario().getSenha(),
                CLIENTE_DEFAULT.getCpf(),
                CLIENTE_DEFAULT.getTelefone(),
                new EnderecoDTO(
                        "rua",
                        "numero",
                        "cidade",
                        "bairro",
                        "estado"),
                CLIENTE_DEFAULT.getEmail(),
                "123456");

        var responseBody = new DadosCompletosClienteDTO(CLIENTE_DEFAULT);
        when(service.salvarClienteComUsuario(any())).thenReturn(responseBody);

        // act
        ControllerTestUtils.postRequest(
                mvc,
                BASE_URL + "/usuario",
                criarUsuarioClienteDTOJson.write(requestBody).getJson())

                // assert
                .andExpect(status().isOk());

        verify(service).salvarClienteComUsuario(any(requestBody.getClass()));
    }

    @Test
    void testCriarClienteUsuario_comBodyInvalido01() throws Exception {
        // arrange
        var requestBody = new CriarUsuarioClienteDTO(
                TestConstants.NOME_VAZIO,
                TestConstants.LOGIN_MUITO_PEQUENO,
                TestConstants.SENHA_MUITO_GRANDE,
                TestConstants.CPF_MUITO_PEQUENO,
                TestConstants.TELEFONE_MUITO_PEQUENO,
                null,
                TestConstants.EMAIL_VAZIO,
                "");

        // act
        ControllerTestUtils.postRequest(
                mvc,
                BASE_URL + "/usuario",
                criarUsuarioClienteDTOJson.write(requestBody).getJson())

                // assert
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields").exists())
                .andExpect(jsonPath("$.fields.nome").exists())
                .andExpect(jsonPath("$.fields.login").exists())
                .andExpect(jsonPath("$.fields.senha").exists())
                .andExpect(jsonPath("$.fields.cpf").exists())
                .andExpect(jsonPath("$.fields.telefone").exists())
                .andExpect(jsonPath("$.fields.email").exists())
                .andExpect(jsonPath("$.fields.verificationCode").exists())
                .andExpect(jsonPath("$.fields.endereco").exists());

        verifyNoInteractions(service);
    }

    @Test
    void testCriarUsuarioCliente_comBodyInvalido02() throws Exception {
        // arrange
        var requestBody = new CriarUsuarioClienteDTO(
                TestConstants.NOME_VAZIO,
                TestConstants.LOGIN_MUITO_GRANDE,
                TestConstants.SENHA_MUITO_GRANDE,
                TestConstants.CPF_MUITO_GRANDE,
                TestConstants.TELEFONE_MUITO_GRANDE,
                null,
                TestConstants.EMAIL_VAZIO,
                "");

        // act
        ControllerTestUtils.postRequest(
                mvc,
                BASE_URL + "/usuario",
                criarUsuarioClienteDTOJson.write(requestBody).getJson())

                // assert
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields").exists())
                .andExpect(jsonPath("$.fields.nome").exists())
                .andExpect(jsonPath("$.fields.login").exists())
                .andExpect(jsonPath("$.fields.senha").exists())
                .andExpect(jsonPath("$.fields.cpf").exists())
                .andExpect(jsonPath("$.fields.telefone").exists())
                .andExpect(jsonPath("$.fields.email").exists())
                .andExpect(jsonPath("$.fields.verificationCode").exists())
                .andExpect(jsonPath("$.fields.endereco").exists());

        verifyNoInteractions(service);
    }

    @TestTemplate
    @ContextualizeUsuarioTypeWithRoles(roles = { "USER", "ADMIN" })
    void testCriarCliente_comRolesAutorizadas() throws IOException, Exception {
        // arrange
        var requestBody = new CriarClienteDTO(
                CLIENTE_DEFAULT.getCpf(),
                CLIENTE_DEFAULT.getNome(),
                CLIENTE_DEFAULT.getTelefone(),
                CLIENTE_DEFAULT.getEmail(),
                new EnderecoDTO(CLIENTE_DEFAULT.getEndereco()));

        var responseBody = new DadosCompletosClienteDTO(CLIENTE_DEFAULT);
        when(service.salvarCliente(any())).thenReturn(responseBody);

        // act
        ControllerTestUtils.postRequest(mvc, BASE_URL, criarClienteDTO.write(requestBody).getJson())
                // assert
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.cpf").exists())
                .andExpect(jsonPath("$.nome").exists())
                .andExpect(jsonPath("$.telefone").exists())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.endereco").exists())
                .andExpect(jsonPath("$.endereco.rua").exists())
                .andExpect(jsonPath("$.endereco.numero").exists())
                .andExpect(jsonPath("$.endereco.cidade").exists())
                .andExpect(jsonPath("$.endereco.bairro").exists())
                .andExpect(jsonPath("$.endereco.estado").exists());

        verify(service).salvarCliente(any());
    }

    @TestTemplate
    @ContextualizeUsuarioTypeWithRoles(roles = { "CLIENT" })
    void testCriarCliente_comRolesNaoAutorizadas() throws IOException, Exception {
        // arrange
        var requestBody = new CriarClienteDTO(
                CLIENTE_DEFAULT.getCpf(),
                CLIENTE_DEFAULT.getNome(),
                CLIENTE_DEFAULT.getTelefone(),
                CLIENTE_DEFAULT.getEmail(),
                new EnderecoDTO(CLIENTE_DEFAULT.getEndereco()));

        var responseBody = new DadosCompletosClienteDTO(CLIENTE_DEFAULT);
        when(service.salvarCliente(any())).thenReturn(responseBody);

        // act
        ControllerTestUtils.postRequest(mvc, BASE_URL, criarClienteDTO.write(requestBody).getJson())
                // assert
                .andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser
    void testCriarCliente_comBodyInvalido() throws IOException, Exception {
        // arrange
        var requestBody = new CriarClienteDTO(
                TestConstants.CPF_MUITO_PEQUENO, // menos de 11 caracteres
                null,
                TestConstants.TELEFONE_MUITO_PEQUENO,
                null,
                null);

        var responseBody = new DadosCompletosClienteDTO(CLIENTE_DEFAULT);
        when(service.salvarCliente(any())).thenReturn(responseBody);

        // act
        ControllerTestUtils.postRequest(mvc, BASE_URL, criarClienteDTO.write(requestBody).getJson())
                // assert
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.cpf").exists())
                .andExpect(jsonPath("$.fields.nome").exists())
                .andExpect(jsonPath("$.fields.telefone").exists())
                .andExpect(jsonPath("$.fields.email").exists())
                .andExpect(jsonPath("$.fields.endereco").exists());

        verifyNoInteractions(service);
    }

    @TestTemplate
    @ContextualizeUsuarioTypeWithRoles(roles = { "USER", "ADMIN" })
    void testListarClientes_comRolesAutorizadas() throws IOException, Exception {
        // arrange
        var responseBody = new PageImpl<>(List.of(new DadosClienteDTO(CLIENTE_DEFAULT)));
        when(service.listarTodosClientes(any(), any())).thenReturn(responseBody);

        // act
        ControllerTestUtils.getRequest(mvc, BASE_URL)
                // assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0]id").exists())
                .andExpect(jsonPath("$.content[0]cpf").exists())
                .andExpect(jsonPath("$.content[0]nome").exists());
    }

    @TestTemplate
    @ContextualizeUsuarioTypeWithRoles(roles = { "CLIENT" })
    void testListarClientes_comRolesNaoAutorizadas() throws IOException, Exception {
        // act
        ControllerTestUtils.getRequest(mvc, BASE_URL)
                // assert
                .andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }

    @TestTemplate
    @ContextualizeUsuarioTypeWithRoles(roles = { "USER", "ADMIN" })
    void testListarClientes_comQueryParams() throws IOException, Exception {
        // arrange
        var PARAM_NOME = "any";
        var responseBody = new PageImpl<>(List.of(new DadosClienteDTO(CLIENTE_DEFAULT)));
        when(service.listarTodosClientes(eq(PARAM_NOME), any(Pageable.class))).thenReturn(responseBody);

        // act
        mvc.perform(get(BASE_URL)
                .param("nome", PARAM_NOME))
                // assert
                .andExpect(status().isOk());

        verify(service).listarTodosClientes(eq(PARAM_NOME), any(Pageable.class));
    }

    @TestTemplate
    @ContextualizeUsuarioTypeWithRoles(roles = { "USER", "ADMIN" })
    void testBuscarCliente_comRolesAutorizadas() throws IOException, Exception {
        // arrange
        var responseBody = new DadosCompletosClienteDTO(CLIENTE_DEFAULT);
        when(service.buscarClientePorId(any())).thenReturn(responseBody);

        // act
        ControllerTestUtils.getRequest(mvc, BASE_URL.concat("/1"))
                // assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.cpf").exists())
                .andExpect(jsonPath("$.nome").exists())
                .andExpect(jsonPath("$.telefone").exists())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.endereco").exists());
    }

    @TestTemplate
    @ContextualizeUsuarioTypeWithRoles(roles = { "CLIENT" })
    void testBuscarCliente_comRolesNaoAutorizadas() throws IOException, Exception {
        // arrange
        var responseBody = new DadosCompletosClienteDTO(CLIENTE_DEFAULT);
        when(service.buscarClientePorId(any())).thenReturn(responseBody);

        // act
        ControllerTestUtils.getRequest(mvc, BASE_URL.concat("/1"))
                // assert
                .andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }

    @TestTemplate
    @ContextualizeUsuarioTypeWithRoles(roles = { "USER", "ADMIN" })
    void testAtualizarCliente_comRolesAutorizadas() throws IOException, Exception {
        // arrange
        var requestBody = new CriarClienteDTO(
                CLIENTE_DEFAULT.getCpf(),
                CLIENTE_DEFAULT.getNome(),
                CLIENTE_DEFAULT.getTelefone(),
                CLIENTE_DEFAULT.getEmail(),
                new EnderecoDTO(CLIENTE_DEFAULT.getEndereco()));

        var responseBody = new DadosCompletosClienteDTO(CLIENTE_DEFAULT);
        when(service.salvarCliente(any())).thenReturn(responseBody);

        // act
        ControllerTestUtils.postRequest(mvc, BASE_URL, criarClienteDTO.write(requestBody).getJson())
                // assert
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.cpf").exists())
                .andExpect(jsonPath("$.nome").exists())
                .andExpect(jsonPath("$.telefone").exists())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.endereco").exists())
                .andExpect(jsonPath("$.endereco.rua").exists())
                .andExpect(jsonPath("$.endereco.numero").exists())
                .andExpect(jsonPath("$.endereco.cidade").exists())
                .andExpect(jsonPath("$.endereco.bairro").exists())
                .andExpect(jsonPath("$.endereco.estado").exists());

        verify(service).salvarCliente(any());
    }

    @TestTemplate
    @ContextualizeUsuarioTypeWithRoles(roles = { "CLIENT" })
    void testAtualizarCliente_comRolesNaoAutorizadas() throws IOException, Exception {
        // arrange
        var requestBody = new AtualizarClienteDTO(
                CLIENTE_DEFAULT.getNome(),
                CLIENTE_DEFAULT.getTelefone(),
                CLIENTE_DEFAULT.getEmail());

        // act
        ControllerTestUtils.putRequest(mvc, BASE_URL, atualizarClienteDTOJson.write(requestBody).getJson())
                // assert
                .andExpect(status().isForbidden());
    }

    @TestTemplate
    @ContextualizeUsuarioTypeWithRoles(roles = { "USER", "ADMIN" })
    void testAtualizarEnderecoCliente_comRolesAutorizadas() throws IOException, Exception {
        // arrange
        var requestBody = new EnderecoDTO(CLIENTE_DEFAULT.getEndereco());
        var responseBody = new DadosCompletosClienteDTO(CLIENTE_DEFAULT);
        when(service.editarEnderecoCliente(anyLong(), any())).thenReturn(responseBody);

        // act
        ControllerTestUtils
                .putRequest(mvc, (BASE_URL.concat("/1/endereco")), enderecoDTOJson.write(requestBody).getJson())
                // assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.cpf").exists())
                .andExpect(jsonPath("$.nome").exists())
                .andExpect(jsonPath("$.telefone").exists())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.endereco").exists())
                .andExpect(jsonPath("$.endereco.rua").exists())
                .andExpect(jsonPath("$.endereco.numero").exists())
                .andExpect(jsonPath("$.endereco.cidade").exists())
                .andExpect(jsonPath("$.endereco.bairro").exists())
                .andExpect(jsonPath("$.endereco.estado").exists());

        verify(service).editarEnderecoCliente(anyLong(), any());
    }

    @TestTemplate
    @ContextualizeUsuarioTypeWithRoles(roles = { "CLIENT" })
    void testAtualizarEnderecoCliente_comRolesNaoAutorizadas() throws IOException, Exception {
        // arrange
        var requestBody = new AtualizarClienteDTO(
                CLIENTE_DEFAULT.getNome(),
                CLIENTE_DEFAULT.getTelefone(),
                CLIENTE_DEFAULT.getEmail());

        // act
        ControllerTestUtils.putRequest(mvc, BASE_URL, atualizarClienteDTOJson.write(requestBody).getJson())
                // assert
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void testAtualizarEnderecoCliente_comBodyInvalido() throws IOException, Exception {
        // arrange
        var requestBody = new EnderecoDTO();
        var responseBody = new DadosCompletosClienteDTO(CLIENTE_DEFAULT);
        when(service.editarEnderecoCliente(anyLong(), any())).thenReturn(responseBody);

        // act
        ControllerTestUtils
                .putRequest(mvc, (BASE_URL.concat("/1/endereco")), enderecoDTOJson.write(requestBody).getJson())
                // assert
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @TestTemplate
    @ContextualizeUsuarioTypeWithRoles(roles = { "USER", "ADMIN" })
    void testDeletarCliente_comRolesAutorizadas() throws IOException, Exception {
        // act
        ControllerTestUtils.deleteMapping(mvc, BASE_URL.concat("/1"))
                // assert
                .andExpect(status().isNoContent());
    }

    @TestTemplate
    @ContextualizeUsuarioTypeWithRoles(roles = { "CLIENT" })
    void testDeletarCliente_comRolesNaoAutorizadas() throws IOException, Exception {
        // act
        ControllerTestUtils.deleteMapping(mvc, BASE_URL.concat("/1"))
                // assert
                .andExpect(status().isForbidden());
    }

    @TestTemplate
    @ContextualizeUsuarioTypeWithRoles(roles = { "CLIENT" })
    void testBuscarClienteMe_comRolesAutorizadas() throws IOException, Exception {
        // arrange
        var responseBody = CLIENTE_DEFAULT;
        when(service.getClienteByUsuarioId(any())).thenReturn(responseBody);

        // act
        ControllerTestUtils.getRequest(mvc, ME_ROUTE)
                // assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.cpf").exists())
                .andExpect(jsonPath("$.nome").exists())
                .andExpect(jsonPath("$.telefone").exists())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.endereco").exists());
    }

    @TestTemplate
    @ContextualizeUsuarioTypeWithRoles(roles = { "USER", "ADMIN" })
    void testBuscarClienteMe_comRolesNaoAutorizadas() throws IOException, Exception {
        // act
        ControllerTestUtils.getRequest(mvc, ME_ROUTE)
                // assert
                .andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }

    @TestTemplate
    @ContextualizeUsuarioTypeWithRoles(roles = { "CLIENT" })
    void atualizarClienteMe_comRolesAutorizadas() throws IOException, Exception {
        // arrange
        var requestBody = new AtualizarClienteDTO(
                CLIENTE_DEFAULT.getNome(),
                CLIENTE_DEFAULT.getTelefone(),
                CLIENTE_DEFAULT.getEmail());

        var responseBody = new DadosCompletosClienteDTO(CLIENTE_DEFAULT);
        when(service.getClienteByUsuarioId(any())).thenReturn(CLIENTE_DEFAULT);
        when(service.editarContatoClienteAtual(any(Cliente.class), any())).thenReturn(responseBody);

        // act
        ControllerTestUtils.putRequest(mvc, ME_ROUTE, atualizarClienteDTOJson.write(requestBody).getJson())
                // assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.cpf").exists())
                .andExpect(jsonPath("$.nome").exists())
                .andExpect(jsonPath("$.telefone").exists())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.endereco").exists())
                .andExpect(jsonPath("$.endereco.rua").exists())
                .andExpect(jsonPath("$.endereco.numero").exists())
                .andExpect(jsonPath("$.endereco.cidade").exists())
                .andExpect(jsonPath("$.endereco.bairro").exists())
                .andExpect(jsonPath("$.endereco.estado").exists());
    }

    @TestTemplate
    @ContextualizeUsuarioTypeWithRoles(roles = { "USER", "ADMIN" })
    void testAtualizarClienteMe_comRolesNaoAutorizadas() throws IOException, Exception {
        // arrange
        var requestBody = new AtualizarClienteDTO(
                CLIENTE_DEFAULT.getNome(),
                CLIENTE_DEFAULT.getTelefone(),
                CLIENTE_DEFAULT.getEmail());

        // act
        ControllerTestUtils.putRequest(mvc, ME_ROUTE, atualizarClienteDTOJson.write(requestBody).getJson())
                // assert
                .andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }

    @TestTemplate
    @ContextualizeUsuarioTypeWithRoles(roles = { "CLIENT" })
    void testAtualizarEnderecoClienteMe_comRolesAutorizadas() throws IOException, Exception {
        // arrange
        var requestBody = new EnderecoDTO(CLIENTE_DEFAULT.getEndereco());
        var responseBody = new DadosCompletosClienteDTO(CLIENTE_DEFAULT);
        when(service.getClienteByUsuarioId(anyLong())).thenReturn(CLIENTE_DEFAULT);
        when(service.editarEnderecoClienteAtual(any(Cliente.class), any())).thenReturn(responseBody);

        // act
        ControllerTestUtils.putRequest(
                mvc,
                (ME_ROUTE.concat("/endereco")),
                enderecoDTOJson.write(requestBody).getJson())
                // assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.cpf").exists())
                .andExpect(jsonPath("$.nome").exists())
                .andExpect(jsonPath("$.telefone").exists())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.endereco").exists())
                .andExpect(jsonPath("$.endereco.rua").exists())
                .andExpect(jsonPath("$.endereco.numero").exists())
                .andExpect(jsonPath("$.endereco.cidade").exists())
                .andExpect(jsonPath("$.endereco.bairro").exists())
                .andExpect(jsonPath("$.endereco.estado").exists());
    }

    @TestTemplate
    @ContextualizeUsuarioTypeWithRoles(roles = { "USER", "ADMIN" })
    void testAtualizarEnderecoClienteMe_comRolesNaoAutorizadas() throws IOException, Exception {
        // arrange
        var requestBody = new AtualizarClienteDTO(
                CLIENTE_DEFAULT.getNome(),
                CLIENTE_DEFAULT.getTelefone(),
                CLIENTE_DEFAULT.getEmail());

        // act
        ControllerTestUtils
                .putRequest(mvc, ME_ROUTE.concat("/endereco"), atualizarClienteDTOJson.write(requestBody).getJson())
                // assert
                .andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }

    @TestTemplate
    @ContextualizeUsuarioTypeWithRoles(roles = { "CLIENT" })
    void testAtualizarEnderecoClienteMe_comBodyInvalido() throws IOException, Exception {
        // arrange
        var requestBody = new EnderecoDTO();
        var responseBody = new DadosCompletosClienteDTO(CLIENTE_DEFAULT);
        when(service.getClienteByUsuarioId(anyLong())).thenReturn(CLIENTE_DEFAULT);
        when(service.editarEnderecoCliente(anyLong(), any())).thenReturn(responseBody);

        // act
        ControllerTestUtils
                .putRequest(mvc, (ME_ROUTE.concat("/endereco")), enderecoDTOJson.write(requestBody).getJson())
                // assert
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }
}