package com.hair.service;

import com.hair.model.Agendamento;
import com.hair.model.Profissional;
import com.hair.model.Servico;
import com.hair.model.Usuario;
import com.hair.repository.AgendamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AgendamentoService {
    
    private final AgendamentoRepository agendamentoRepository;
    private final ProfissionalService profissionalService;
    private final ServicoService servicoService;
    
    private static final String HORA_ABERTURA = "09:00";
    private static final String HORA_FECHAMENTO = "21:00";
    
    public Agendamento salvar(Agendamento agendamento) {
        validarAgendamento(agendamento);
        agendamento.setDataMarcacao(LocalDateTime.now());
        if (agendamento.getStatus() == null) {
            agendamento.setStatus(Agendamento.StatusAgendamento.AGENDADO);
        }
        return agendamentoRepository.save(agendamento);
    }
    
    public Optional<Agendamento> buscarPorId(Long id) {
        return agendamentoRepository.findById(id);
    }
    
    public List<Agendamento> buscarTodos() {
        return agendamentoRepository.findAll();
    }
    
    public List<Agendamento> buscarPorProfissional(Long profissionalId) {
        Optional<Profissional> profissional = profissionalService.buscarPorId(profissionalId);
        return profissional.map(agendamentoRepository::findByProfissionalOrderByDataAgendamento)
                           .orElse(new ArrayList<>());
    }
    
    public List<Agendamento> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return agendamentoRepository.findByDataAgendamentoBetween(inicio, fim);
    }
    
    public List<Agendamento> buscarPorUsuario(String login) {
        return agendamentoRepository.findByUsuarioLogin(login);
    }
    
    public List<String> getHorariosDisponiveis(LocalDateTime data, Long profissionalId) {
        List<String> horariosOcupados;
        
        if (profissionalId != null) {
            horariosOcupados = agendamentoRepository.findHorariosOcupadosByProfissionalAndData(profissionalId, data);
        } else {
            horariosOcupados = agendamentoRepository.findHorariosOcupadosByData(data);
        }
        
        List<String> todosHorarios = gerarHorariosTrabalho();
        todosHorarios.removeAll(horariosOcupados);
        
        return todosHorarios;
    }
    
    public List<Profissional> getProfissionaisDisponiveis(LocalDateTime data, String horario) {
        return profissionalService.buscarDisponiveis(data, horario);
    }
    
    public void cancelar(Long id) {
        Optional<Agendamento> agendamento = buscarPorId(id);
        if (agendamento.isPresent()) {
            agendamento.get().setStatus(Agendamento.StatusAgendamento.CANCELADO);
            agendamentoRepository.save(agendamento.get());
        } else {
            throw new RuntimeException("Agendamento não encontrado com ID: " + id);
        }
    }
    
    public void confirmar(Long id) {
        Optional<Agendamento> agendamento = buscarPorId(id);
        if (agendamento.isPresent()) {
            agendamento.get().setStatus(Agendamento.StatusAgendamento.CONFIRMADO);
            agendamentoRepository.save(agendamento.get());
        } else {
            throw new RuntimeException("Agendamento não encontrado com ID: " + id);
        }
    }
    
    public void deletar(Long id) {
        if (agendamentoRepository.existsById(id)) {
            agendamentoRepository.deleteById(id);
        } else {
            throw new RuntimeException("Agendamento não encontrado com ID: " + id);
        }
    }
    
    private void validarAgendamento(Agendamento agendamento) {
        if (agendamento.getProfissional() == null || agendamento.getServico() == null) {
            throw new RuntimeException("Profissional e serviço são obrigatórios");
        }
        
        if (!isHorarioValido(agendamento.getHorarioAgendado())) {
            throw new RuntimeException("Horário deve estar entre 09:00 e 21:00");
        }
        
        boolean existeConflito = agendamentoRepository.existsByProfissionalIdAndDataAgendamentoAndHorarioAgendado(
            agendamento.getProfissional().getId(),
            agendamento.getDataAgendamento(),
            agendamento.getHorarioAgendado()
        );
        
        if (existeConflito) {
            throw new RuntimeException("Já existe um agendamento para este profissional neste horário");
        }
    }
    
    private boolean isHorarioValido(String horario) {
        return horario.compareTo(HORA_ABERTURA) >= 0 && horario.compareTo(HORA_FECHAMENTO) < 0;
    }
    
    private List<String> gerarHorariosTrabalho() {
        List<String> horarios = new ArrayList<>();
        for (int hora = 9; hora < 21; hora++) {
            horarios.add(String.format("%02d:00", hora));
            horarios.add(String.format("%02d:30", hora));
        }
        return horarios;
    }
}
