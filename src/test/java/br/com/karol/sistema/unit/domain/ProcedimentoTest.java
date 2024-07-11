package br.com.karol.sistema.unit.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import br.com.karol.sistema.domain.Procedimento;

public class ProcedimentoTest {

    private final String NOME = "Nome";
    private final String DESCRICAO = "Descricao";
    private final LocalTime DURACAO = LocalTime.now();
    private final Double VALOR = Double.valueOf(50.00);

    private final Double VALOR_INVALIDO = VALOR - 1.00;


    @Test
    void criarComAtributosValidosTest() {
        assertDoesNotThrow(() -> new Procedimento(NOME, DESCRICAO, DURACAO, VALOR));
    }

    @Test
    void naoDeveCriarProcedimentoComValoresNullTest() {
        assertThrows(NullPointerException.class, () -> new Procedimento(null, null, null, null));
        assertThrows(NullPointerException.class, () -> new Procedimento(null, DESCRICAO, DURACAO, VALOR));
        assertThrows(NullPointerException.class, () -> new Procedimento(NOME, null, DURACAO, VALOR));
        assertThrows(NullPointerException.class, () -> new Procedimento(NOME, DESCRICAO, null, VALOR));
        assertThrows(NullPointerException.class, () -> new Procedimento(NOME, DESCRICAO, DURACAO, null));
    }

    @Test
    void naoDeveCriarProcedimentoComValoresBlankTest() {
        assertThrows(IllegalArgumentException.class, () -> new Procedimento("", DESCRICAO, DURACAO, VALOR));
        assertThrows(IllegalArgumentException.class, () -> new Procedimento(NOME, "", DURACAO, VALOR));
    }

    @Test
    void deveLancarExcecaoAoTentarCriarProcedimentoComAtributoValorAbaixoDoValorMinimoTest() {
        assertThrows(IllegalArgumentException.class, () -> new Procedimento(NOME, DESCRICAO, DURACAO, VALOR_INVALIDO));
    }

    @Test
    void deveAtualizarDadosTest() {
        // arrange
        Procedimento test = this.getProcedimento();

        // act and assert
        assertDoesNotThrow(() -> test.atualizarDados(NOME, DESCRICAO, DURACAO, VALOR));

        assertEquals(NOME, test.getNome());
        assertEquals(DESCRICAO, test.getDescricao());
        assertEquals(DURACAO, test.getDuracao());
        assertEquals(VALOR, test.getValor());
    }

    @Test
    void atualizarDadosComStringsBlankDeveLancarExceptionTest() {
        // arrange
        Procedimento test = new Procedimento();

        // act and assert
        assertDoesNotThrow(() -> test.atualizarDados("", DESCRICAO, DURACAO, VALOR));
        assertDoesNotThrow(() -> test.atualizarDados(NOME, "", DURACAO, VALOR));
    }

    @Test
    void atualizarDadosComAtributoValorAbaixoDoValorMinimoDeveLancarExceptionTest() {
        // arrange
        Procedimento test = new Procedimento();

        // act and assert
        assertThrows(IllegalArgumentException.class, () -> test.atualizarDados(NOME, DESCRICAO, DURACAO, VALOR_INVALIDO));
    }

    private Procedimento getProcedimento() {
        Procedimento procedimento = new Procedimento();
        ReflectionTestUtils.setField(procedimento, "nome", "umNome");
        ReflectionTestUtils.setField(procedimento, "descricao", "umaDescricao");
        ReflectionTestUtils.setField(procedimento, "duracao", LocalTime.now().plusHours(10));
        ReflectionTestUtils.setField(procedimento, "valor", VALOR + 50.00);
        return procedimento;
    }
}