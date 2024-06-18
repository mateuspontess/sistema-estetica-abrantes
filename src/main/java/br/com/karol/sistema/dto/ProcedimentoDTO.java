package br.com.karol.sistema.dto;

import br.com.karol.sistema.domain.Procedimento;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class ProcedimentoDTO {

    private Integer id;
    private String nome;
    private String descricao;
    private Double valor;

    public ProcedimentoDTO(Procedimento procedimento) {
        BeanUtils.copyProperties(procedimento, this);
    }


}
