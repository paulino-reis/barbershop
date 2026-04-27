package com.hair.controller;

import com.hair.dto.AgendamentoDTO;
import com.hair.dto.EstatisticasProfissionalDTO;
import com.hair.dto.HorarioOcupadoDTO;
import com.hair.dto.RelatorioFinanceiroDTO;
import com.hair.model.Agendamento;
import com.hair.model.Profissional;
import com.hair.model.Usuario;
import com.hair.service.AgendamentoService;
import com.hair.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/agendamentos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AgendamentoController {
    
    private final AgendamentoService agendamentoService;
    private final UsuarioService usuarioService;

    @GetMapping("/horarios-disponiveis")
    public ResponseEntity<List<String>> getHorariosDisponiveis(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime data,
            @RequestParam(required = false) Long profissionalId) {
        return ResponseEntity.ok(agendamentoService.getHorariosDisponiveis(data, profissionalId));
    }
    
    @GetMapping("/horarios-ocupados")
    public ResponseEntity<List<HorarioOcupadoDTO>> getHorariosOcupados(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime data,
            @RequestParam(required = false) Long profissionalId) {
        return ResponseEntity.ok(agendamentoService.getHorariosOcupados(data, profissionalId));
    }
    
    @GetMapping("/profissionais-disponiveis")
    public ResponseEntity<List<Profissional>> getProfissionaisDisponiveis(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime data,
            @RequestParam String horario) {
        return ResponseEntity.ok(agendamentoService.getProfissionaisDisponiveis(data, horario));
    }
    
    @GetMapping
    public ResponseEntity<List<Agendamento>> buscarTodos(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.ok(List.of());
        }
        String login = authentication.getName();
        return ResponseEntity.ok(agendamentoService.buscarPorUsuario(login));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Agendamento> buscarPorId(@PathVariable Long id) {
        return agendamentoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/profissional/{profissionalId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Agendamento>> buscarPorProfissional(@PathVariable Long profissionalId) {
        return ResponseEntity.ok(agendamentoService.buscarPorProfissional(profissionalId));
    }
    
    @GetMapping("/periodo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Agendamento>> buscarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        return ResponseEntity.ok(agendamentoService.buscarPorPeriodo(inicio, fim));
    }
    
    @GetMapping("/estatisticas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EstatisticasProfissionalDTO>> getEstatisticasPorProfissional(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        return ResponseEntity.ok(agendamentoService.getEstatisticasPorProfissional(inicio, fim));
    }

    @GetMapping("/relatorio-financeiro")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RelatorioFinanceiroDTO>> getRelatorioFinanceiro(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim,
            @RequestParam(required = false) Long profissionalId) {
        return ResponseEntity.ok(agendamentoService.getRelatorioFinanceiro(inicio, fim, profissionalId));
    }

    @GetMapping("/por-profissional-periodo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Agendamento>> buscarAgendamentosPorProfissionalEPeriodo(
            @RequestParam Long profissionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        return ResponseEntity.ok(agendamentoService.buscarPorProfissionalEPeriodo(profissionalId, inicio, fim));
    }
    
    @GetMapping("/hoje")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Agendamento>> buscarAgendamentosHoje() {
        return ResponseEntity.ok(agendamentoService.buscarAgendamentosHoje());
    }
    
    @GetMapping("/por-data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Agendamento>> buscarAgendamentosPorData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate data) {
        return ResponseEntity.ok(agendamentoService.buscarAgendamentosPorData(data));
    }
    
    @PostMapping
    public ResponseEntity<Agendamento> salvar(@Valid @RequestBody AgendamentoDTO agendamentoDTO, 
                                             Authentication authentication) {
        try {
            Agendamento saved = agendamentoService.salvar(agendamentoDTO, authentication);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Agendamento> atualizar(
            @PathVariable Long id, @Valid @RequestBody AgendamentoDTO agendamentoDTO) {
        if (agendamentoService.buscarPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(agendamentoService.atualizar(id, agendamentoDTO));
    }
    
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable Long id, Authentication authentication) {
        String login = authentication.getName();
        Usuario usuario = usuarioService.buscarPorLogin(login)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        agendamentoService.cancelar(id, usuario.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        agendamentoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
