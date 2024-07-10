package br.com.karol.sistema.business.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.karol.sistema.domain.Agendamento;
import br.com.karol.sistema.domain.Procedimento;
import br.com.karol.sistema.domain.constants.AgendamentoConstants;
import br.com.karol.sistema.infra.repository.AgendamentoRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class DisponibilidadeService {

    private static final Integer INTERVALO = AgendamentoConstants.INTERVALO_ENTRE_AGENDAMENTOS_EM_MINUTOS;

    private AgendamentoRepository agendamentoRepository;
    private ProcedimentoService procedimentoService;

        
    public List<LocalDateTime> filtrarHorariosDisponiveis(String procedimentoId, LocalDate dataHora) {
        Procedimento procedimento = this.procedimentoService.getProcedimentoById(procedimentoId);
        List<Agendamento> agendamentos = agendamentoRepository.findBetweenDataHora(
            dataHora.atStartOfDay(), 
            dataHora.atTime(23, 59, 59));

        List<LocalDateTime> horariosDisponiveis = new ArrayList<>();
        LocalTime horarioAbertura = LocalTime.of(8, 0);
        LocalTime horarioFechamento = LocalTime.of(18, 0);

        LocalTime horarioAtual = horarioAbertura
            .plusHours(procedimento.getDuracao().getHour())
            .plusMinutes(procedimento.getDuracao().getMinute())
            .plusMinutes(INTERVALO);
        while (horarioAtual.isBefore(horarioFechamento)) {
            boolean disponivel = true;
            LocalDateTime inicioNovoAgendamento = dataHora.atTime(horarioAtual);
            LocalDateTime terminoNovoAgendamento = inicioNovoAgendamento
                .plusHours(procedimento.getDuracao().getHour())
                .plusMinutes(procedimento.getDuracao().getMinute());

            for (Agendamento agendamento : agendamentos) {
                LocalDateTime inicioAgendamentoAtual = agendamento.getDataHora();
                LocalDateTime terminoAgendamentoAtual = inicioAgendamentoAtual
                    .plusHours(agendamento.getProcedimento().getDuracao().getHour())
                    .plusMinutes(agendamento.getProcedimento().getDuracao().getMinute())
                    .plusMinutes(INTERVALO);
                
                if (inicioNovoAgendamento.isBefore(terminoAgendamentoAtual) && terminoNovoAgendamento.isAfter(inicioAgendamentoAtual)) {
                    disponivel = false;
                    break;
                }
            }

            if (disponivel) {
                horariosDisponiveis.add(inicioNovoAgendamento);
            }

            horarioAtual = horarioAtual.plusMinutes(30);
        }
        return horariosDisponiveis;
    }
}