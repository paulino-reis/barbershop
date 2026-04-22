package com.hair.controller;

import com.hair.dto.ServicoDTO;
import com.hair.model.Servico;
import com.hair.service.ServicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ServicoController {
    
    private final ServicoService servicoService;
    
    @GetMapping
    public ResponseEntity<List<Servico>> buscarTodos() {
        return ResponseEntity.ok(servicoService.buscarTodos());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Servico> buscarPorId(@PathVariable Long id) {
        return servicoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/id-servico/{idServico}")
    public ResponseEntity<Servico> buscarPorIdServico(@PathVariable String idServico) {
        return servicoService.buscarPorIdServico(idServico)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/faixa-preco")
    public ResponseEntity<List<Servico>> buscarPorFaixaPreco(
            @RequestParam Double precoMin, @RequestParam Double precoMax) {
        return ResponseEntity.ok(servicoService.buscarPorFaixaPreco(precoMin, precoMax));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Servico> salvar(@Valid @RequestBody ServicoDTO servicoDTO) {
        return ResponseEntity.ok(servicoService.salvar(servicoDTO));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Servico> atualizar(
            @PathVariable Long id, @Valid @RequestBody ServicoDTO servicoDTO) {
        if (!servicoService.existePorId(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(servicoService.atualizar(id, servicoDTO));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        servicoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
