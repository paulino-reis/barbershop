package com.hair.controller;

import com.hair.dto.EstatisticasProfissionalDTO;
import com.hair.dto.HorarioOcupadoDTO;
import com.hair.model.Agendamento;
import com.hair.model.Profissional;
import com.hair.service.AgendamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agendamentos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AgendamentoController {
    
    private final AgendamentoService agendamentoService;

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
    
    @PostMapping
    public ResponseEntity<Agendamento> salvar(@Valid @RequestBody Agendamento agendamento, 
                                             Authentication authentication) {
        return ResponseEntity.ok(agendamentoService.salvar(agendamento, authentication));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Agendamento> atualizar(
            @PathVariable Long id, @Valid @RequestBody Agendamento agendamento) {
        if (agendamentoService.buscarPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        agendamento.setId(id);
        return ResponseEntity.ok(agendamentoService.salvar(agendamento));
    }
    
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        agendamentoService.cancelar(id);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{id}/confirmar")
    public ResponseEntity<Map<String, String>> confirmar(@PathVariable Long id) {
        // Check if user is admin or owns the appointment
        Agendamento agendamento = agendamentoService.buscarPorId(id).orElse(null);
        if (agendamento == null) {
            return ResponseEntity.notFound().build();
        }
        
        String whatsappLink = agendamentoService.confirmar(id);
        Map<String, String> response = new java.util.HashMap<>();
        response.put("whatsappLink", whatsappLink);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        agendamentoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
