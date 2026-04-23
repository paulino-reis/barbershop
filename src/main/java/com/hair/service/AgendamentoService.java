package com.hair.service;

import com.hair.dto.AgendamentoDTO;
import com.hair.dto.EstatisticasProfissionalDTO;
import com.hair.dto.HorarioOcupadoDTO;
import com.hair.exception.AgendamentoNotFoundException;
import com.hair.exception.ProfissionalNotFoundException;
import com.hair.exception.ServicoNotFoundException;
import com.hair.exception.ValidationException;
import com.hair.model.Agendamento;
import com.hair.model.Profissional;
import com.hair.model.Usuario;
import com.hair.repository.AgendamentoRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AgendamentoService {
    
    private final AgendamentoRepository agendamentoRepository;
    private final ProfissionalService profissionalService;
    private final ServicoService servicoService;
    private final UsuarioService usuarioService;
    
    private static final String HORA_ABERTURA = "09:00";
    private static final String HORA_FECHAMENTO = "21:00";
    
    public Agendamento salvar(AgendamentoDTO agendamentoDTO, Authentication authentication) {
        Agendamento agendamento = new Agendamento();
        preencherAgendamentoFromDTO(agendamento, agendamentoDTO);
        return salvar(agendamento, authentication);
    }

    public Agendamento salvar(Agendamento agendamento, Authentication authentication) {
        // Associate authenticated user with the appointment
        if (authentication != null && authentication.isAuthenticated()) {
            String login = authentication.getName();
            Usuario usuario = usuarioService.buscarPorLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + login));
            agendamento.setUsuario(usuario);
        }
        
        validarAgendamento(agendamento);
        agendamento.setDataMarcacao(LocalDateTime.now());
        if (agendamento.getStatus() == null) {
            agendamento.setStatus(Agendamento.StatusAgendamento.AGENDADO);
        }
        return agendamentoRepository.save(agendamento);
    }
    
    // Overloaded method for internal use without Authentication
    public Agendamento salvar(Agendamento agendamento) {
        return salvar(agendamento, null);
    }
    
    public Agendamento atualizar(Long id, AgendamentoDTO agendamentoDTO) {
        Agendamento agendamento = buscarPorId(id)
            .orElseThrow(() -> new AgendamentoNotFoundException(id));
        
        preencherAgendamentoFromDTO(agendamento, agendamentoDTO);
        
        validarAgendamento(agendamento);
        return agendamentoRepository.save(agendamento);
    }
    
    public Optional<Agendamento> buscarPorId(Long id) {
        return agendamentoRepository.findById(id);
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
        List<Agendamento> agendamentos = agendamentoRepository.findByUsuarioLogin(login);
        agendamentos.forEach(ag -> {
            if (ag.getCanceledByUserId() != null) {
                usuarioService.buscarPorId(ag.getCanceledByUserId()).ifPresent(usuario -> ag.setCanceledByUserName(usuario.getNomeUsuario()));
            }
        });
        return agendamentos;
    }
    
    public List<Agendamento> buscarAgendamentosHoje() {
        LocalDateTime hojeInicio = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime hojeFim = hojeInicio.plusDays(1);
        return agendamentoRepository.findByDataAgendamentoBetween(hojeInicio, hojeFim);
    }
    
    public List<Agendamento> buscarAgendamentosPorData(java.time.LocalDate data) {
        LocalDateTime dataInicio = data.atStartOfDay();
        LocalDateTime dataFim = dataInicio.plusDays(1);
        return agendamentoRepository.findByDataAgendamentoBetween(dataInicio, dataFim);
    }
    
    public List<String> getHorariosDisponiveis(LocalDateTime data, Long profissionalId) {
        LocalDateTime dataInicio = data.toLocalDate().atStartOfDay();
        LocalDateTime dataFim = dataInicio.plusDays(1);
        
        List<Agendamento> agendamentosOcupados;
        
        if (profissionalId != null) {
            agendamentosOcupados = agendamentoRepository.findHorariosOcupadosByProfissionalAndData(profissionalId, dataInicio, dataFim);
        } else {
            agendamentosOcupados = agendamentoRepository.findHorariosOcupadosByData(dataInicio, dataFim);
        }
        
        List<HorarioOcupadoDTO> horariosOcupados = agendamentosOcupados.stream()
            .filter(a -> a.getUsuario() != null)
            .map(a -> new HorarioOcupadoDTO(
                a.getHorarioAgendado(),
                a.getUsuario().getNomeUsuario(),
                a.getUsuario().getTelefone()
            ))
            .toList();
        
        List<String> todosHorarios;
        if (profissionalId != null) {
            Optional<Profissional> profissional = profissionalService.buscarPorId(profissionalId);
            todosHorarios = profissional.map(p -> gerarHorariosTrabalho(p.getHoraInicio(), p.getHoraFim()))
                                     .orElse(gerarHorariosTrabalho());
        } else {
            todosHorarios = gerarHorariosTrabalho();
        }
        
        List<String> horariosOcupadosStrings = horariosOcupados.stream()
            .map(HorarioOcupadoDTO::getHorario)
            .toList();
        
        todosHorarios.removeAll(horariosOcupadosStrings);
        
        return todosHorarios;
    }
    
    public List<HorarioOcupadoDTO> getHorariosOcupados(LocalDateTime data, Long profissionalId) {
        LocalDateTime dataInicio = data.toLocalDate().atStartOfDay();
        LocalDateTime dataFim = dataInicio.plusDays(1);
        
        List<Agendamento> agendamentosOcupados;
        if (profissionalId != null) {
            agendamentosOcupados = agendamentoRepository.findHorariosOcupadosByProfissionalAndData(profissionalId, dataInicio, dataFim);
        } else {
            agendamentosOcupados = agendamentoRepository.findHorariosOcupadosByData(dataInicio, dataFim);
        }
        
        return agendamentosOcupados.stream()
            .filter(a -> a.getUsuario() != null)
            .map(a -> new HorarioOcupadoDTO(
                a.getHorarioAgendado(),
                a.getUsuario().getNomeUsuario(),
                a.getUsuario().getTelefone()
            ))
            .toList();
    }
    
    public List<Profissional> getProfissionaisDisponiveis(LocalDateTime data, String horario) {
        return profissionalService.buscarDisponiveis(data, horario);
    }
    
    public void cancelar(Long id, Long userId) {
        Optional<Agendamento> agendamento = buscarPorId(id);
        if (agendamento.isPresent()) {
            Agendamento ag = agendamento.get();
            ag.setStatus(Agendamento.StatusAgendamento.CANCELADO);
            ag.setCanceledByUserId(userId);
            ag.setCancellationDate(LocalDateTime.now());
            agendamentoRepository.save(ag);
        } else {
            throw new AgendamentoNotFoundException(id);
        }
    }
    
    public String confirmar(Long id) {
        Optional<Agendamento> agendamento = buscarPorId(id);
        if (agendamento.isPresent()) {
            Agendamento ag = agendamento.get();
            ag.setStatus(Agendamento.StatusAgendamento.CONFIRMADO);
            agendamentoRepository.save(ag);
            
            // Generate WhatsApp link
            return gerarLinkWhatsApp(ag);
        } else {
            throw new AgendamentoNotFoundException(id);
        }
    }
    
    private String gerarLinkWhatsApp(Agendamento agendamento) {
        Usuario usuario = agendamento.getUsuario();
        if (usuario == null) {
            return null;
        }
        
        // Format phone number (remove non-numeric characters and add 55 for Brazil)
        String telefone = usuario.getTelefone().replaceAll("\\D", "");
        if (!telefone.startsWith("55")) {
            telefone = "55" + telefone;
        }
        
        // Format date and time
        String dataFormatada = agendamento.getDataAgendamento().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String mensagem = getMensagem(agendamento, dataFormatada);

        // Encode message for URL
        mensagem = java.net.URLEncoder.encode(mensagem, StandardCharsets.UTF_8);

        // Generate WhatsApp link
        return "https://wa.me/" + telefone + "?text=" + mensagem;
    }

    private static @NonNull String getMensagem(Agendamento agendamento, String dataFormatada) {
        String horario = agendamento.getHorarioAgendado();
        Usuario usuario = agendamento.getUsuario();
        String cliente = usuario != null ? usuario.getNomeUsuario() : "N/A";
        String servico = agendamento.getServico().getNome();
        String profissional = agendamento.getProfissional().getNome();

        // Create message
        return String.format(
            "Cliente *%s* agendou o serviço *%s* para o dia *%s* às *%s* para o profissional *%s*.",
            cliente, servico, dataFormatada, horario, profissional
        );
    }

    public void deletar(Long id) {
        if (agendamentoRepository.existsById(id)) {
            agendamentoRepository.deleteById(id);
        } else {
            throw new AgendamentoNotFoundException(id);
        }
    }
    
    private void validarAgendamento(Agendamento agendamento) {
        if (!isHorarioValido(agendamento.getHorarioAgendado())) {
            throw new ValidationException("Horário deve estar entre 09:00 e 21:00");
        }
        
        boolean existeConflito = agendamentoRepository.existsByProfissionalIdAndDataAgendamentoAndHorarioAgendado(
            agendamento.getProfissional().getId(),
            agendamento.getDataAgendamento(),
            agendamento.getHorarioAgendado()
        );
        
        if (existeConflito) {
            throw new ValidationException("Já existe um agendamento para este profissional neste horário");
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
    
    private List<String> gerarHorariosTrabalho(String horaInicio, String horaFim) {
        if (horaInicio == null || horaFim == null) {
            return gerarHorariosTrabalho();
        }
        
        List<String> horarios = new ArrayList<>();
        int horaInicioInt = Integer.parseInt(horaInicio.split(":")[0]);
        int horaFimInt = Integer.parseInt(horaFim.split(":")[0]);
        
        for (int hora = horaInicioInt; hora < horaFimInt; hora++) {
            horarios.add(String.format("%02d:00", hora));
            horarios.add(String.format("%02d:30", hora));
        }
        return horarios;
    }

    private void preencherAgendamentoFromDTO(Agendamento agendamento, AgendamentoDTO agendamentoDTO) {
        agendamento.setProfissional(profissionalService.buscarPorId(agendamentoDTO.getProfissionalId())
            .orElseThrow(() -> new ProfissionalNotFoundException(agendamentoDTO.getProfissionalId())));
        agendamento.setServico(servicoService.buscarPorId(agendamentoDTO.getServicoId())
            .orElseThrow(() -> new ServicoNotFoundException(agendamentoDTO.getServicoId())));
        agendamento.setDataAgendamento(agendamentoDTO.getDataAgendamento());
        agendamento.setHorarioAgendado(agendamentoDTO.getHorarioAgendado());
        
        if (agendamentoDTO.getStatus() != null) {
            agendamento.setStatus(Agendamento.StatusAgendamento.valueOf(agendamentoDTO.getStatus()));
        }
    }

    public List<EstatisticasProfissionalDTO> getEstatisticasPorProfissional(LocalDateTime dataInicio, LocalDateTime dataFim) {
        List<Agendamento> agendamentos = agendamentoRepository.findByDataAgendamentoBetween(dataInicio, dataFim);
        
        Map<String, Long> estatisticas = agendamentos.stream()
            .filter(a -> a.getStatus() == Agendamento.StatusAgendamento.AGENDADO)
            .collect(Collectors.groupingBy(
                a -> a.getProfissional().getNome(),
                Collectors.counting()
            ));
        
        return estatisticas.entrySet().stream()
            .map(entry -> new EstatisticasProfissionalDTO(entry.getKey(), entry.getValue()))
            .toList();
    }
}
