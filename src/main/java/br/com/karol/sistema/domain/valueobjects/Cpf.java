package br.com.karol.sistema.domain.valueobjects;

import java.util.List;

import br.com.karol.sistema.domain.formatter.CpfFormatter;
import br.com.karol.sistema.domain.validator.cliente.cpf.CpfValidator;
import br.com.karol.sistema.infra.exceptions.FieldValidationException;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Cpf {

    private String value;

    public Cpf(String value, List<CpfValidator> validators, CpfFormatter formatter) {
        if (value == null || value.isBlank()) throw new FieldValidationException(this.getClass().getSimpleName(), "Não pode ser null/blank");
        
        if (validators == null || validators.size() == 0) throw new RuntimeException("Deve fornecedor um ou mais validadores");
        if (formatter == null) throw new RuntimeException("Deve fornecedor um formatador");
        validators.forEach(v -> v.validate(value));

        this.value = formatter.format(value);
    }
}