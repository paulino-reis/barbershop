package com.hair.controller;

import com.hair.model.Profissional;
import com.hair.service.ProfissionalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/profissionais")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProfissionalController {
    
    private final ProfissionalService profissionalService;
    
    @GetMapping
    public ResponseEntity<List<Profissional>> buscarTodos() {
        return ResponseEntity.ok(profissionalService.buscarTodos());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Profissional> buscarPorId(@PathVariable Long id) {
        return profissionalService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/disponiveis")
    public ResponseEntity<List<Profissional>> buscarDisponiveis(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime data,
            @RequestParam String horario) {
        return ResponseEntity.ok(profissionalService.buscarDisponiveis(data, horario));
    }
    
    @GetMapping("/nome/{nome}")
    public ResponseEntity<List<Profissional>> buscarPorNome(@PathVariable String nome) {
        return ResponseEntity.ok(profissionalService.buscarPorNome(nome));
    }
    
    @GetMapping("/cidade/{cidade}/uf/{uf}")
    public ResponseEntity<List<Profissional>> buscarPorCidadeUf(
            @PathVariable String cidade, @PathVariable String uf) {
        return ResponseEntity.ok(profissionalService.buscarPorCidadeUf(cidade, uf));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Profissional> salvar(@Valid @RequestBody Profissional profissional) {
        return ResponseEntity.ok(profissionalService.salvar(profissional));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Profissional> atualizar(
            @PathVariable Long id, @Valid @RequestBody Profissional profissional) {
        if (!profissionalService.existePorId(id)) {
            return ResponseEntity.notFound().build();
        }
        profissional.setId(id);
        return ResponseEntity.ok(profissionalService.salvar(profissional));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        profissionalService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
