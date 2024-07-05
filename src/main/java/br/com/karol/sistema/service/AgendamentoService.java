package br.com.karol.sistema.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.karol.sistema.domain.Agendamento;
import br.com.karol.sistema.domain.Cliente;
import br.com.karol.sistema.domain.Procedimento;
import br.com.karol.sistema.domain.Usuario;
import br.com.karol.sistema.dto.agendamento.AtualizarAgendamentoDTO;
import br.com.karol.sistema.dto.agendamento.CriaAgendamentoDTO;
import br.com.karol.sistema.dto.agendamento.DadosAgendamentoDTO;
import br.com.karol.sistema.exceptions.EntityNotFoundException;
import br.com.karol.sistema.mapper.AgendamentoMapper;
import br.com.karol.sistema.repository.AgendamentoRepository;

@Service
@Transactional
public class AgendamentoService {

    private final String NOT_FOUND_DEFAULT_MESSAGE = "Agendamento não encontrado";

    private AgendamentoRepository agendamentoRepository;
    private ClienteService clienteService;
    private ProcedimentoService procedimentoService;
    private AgendamentoMapper mapper;

    public AgendamentoService(
        AgendamentoRepository repository,
        ClienteService clienteService,
        ProcedimentoService procedimentoService,
        AgendamentoMapper mapper) 
        {
        this.agendamentoRepository = repository;
        this.clienteService = clienteService;
        this.procedimentoService = procedimentoService;
        this.mapper = mapper;
    }

    
    /* ! isso precisará ser readaptado !
     * 
     * É necessário fazer validações mais específicas sobre horários de agendamentos. É necessário levar em conta coisas como:
     * - Horário de abertura
     * - Horário de fechamento 
     * - Intervalo de tempo entre os agendamentos, considerando agendamentos anteriores e futuros dentro de um determinado tempo
     */
    public DadosAgendamentoDTO salvarAgendamento(CriaAgendamentoDTO dadosAgendamento, Usuario usuario) {
        // ----- validações ----- 
        
        Cliente clienteAlvo = clienteService.buscarPorId(dadosAgendamento.getClienteId());
        Procedimento procedimentoAlvo = procedimentoService.getProcedimentoById(dadosAgendamento.getProcedimentoId());

        Agendamento novoAgendamento = new Agendamento(
            procedimentoAlvo,
            dadosAgendamento.getObservacao(),
            clienteAlvo,
            dadosAgendamento.getDataHora(),
            usuario);
        return mapper.toDadosAgendamentoDTO(agendamentoRepository.save(novoAgendamento));
    }

    public DadosAgendamentoDTO buscarAgendamentoPorId(String id) {
        return mapper.toDadosAgendamentoDTO(this.getAgendamentoById(id));
    }

    public List<DadosAgendamentoDTO> listarTodosAgendamentos() {
        return mapper.toListDadosAgentamentoDTO(agendamentoRepository.findAll());
    }

    /* Aqui deve ser implementado na entidade um método de update, precisa-se decidir sobre quais atributos de um agendamento
     * que podem ser modificados.
     * 
     * Se for possível alterar o horário do agendamento será necessário rodar as validações de horário assim como foi feito 
     * na criação de um agendamento
     */
    public DadosAgendamentoDTO  atualizarAgendamento(String id, AtualizarAgendamentoDTO dadosAtualizacao) {
        Agendamento agendamento = this.getAgendamentoById(id);
        agendamento.remarcarAgendamento(dadosAtualizacao.getObservacao(), dadosAtualizacao.getDataHora());
        
        agendamentoRepository.save(agendamento);
        return mapper.toDadosAgendamentoDTO(agendamento);
    }

    public void deletarAgendamento(String agendamentoId) {
        if (!agendamentoRepository.existsById(agendamentoId))
            throw new EntityNotFoundException(this.NOT_FOUND_DEFAULT_MESSAGE);
        agendamentoRepository.deleteById(agendamentoId);
    }

    private Agendamento getAgendamentoById(String id) {
        return agendamentoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(this.NOT_FOUND_DEFAULT_MESSAGE));
    }
}