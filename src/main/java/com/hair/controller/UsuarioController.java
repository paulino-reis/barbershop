package com.hair.controller;

import com.hair.model.Usuario;
import com.hair.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UsuarioController {
    
    private final UsuarioService usuarioService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Usuario>> buscarTodos() {
        return ResponseEntity.ok(usuarioService.buscarTodos());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable Long id, Authentication authentication) {
        String login = authentication.getName();
        Usuario usuario = usuarioService.buscarPorId(id).orElse(null);
        
        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (!login.equals(usuario.getLogin()) && !authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(usuario);
    }
    
    @GetMapping("/perfil")
    public ResponseEntity<Usuario> buscarPerfil(Authentication authentication) {
        String login = authentication.getName();
        return usuarioService.buscarPorLogin(login)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/cadastrar")
    public ResponseEntity<Usuario> cadastrar(@Valid @RequestBody Usuario usuario) {
        return ResponseEntity.ok(usuarioService.salvar(usuario));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> atualizar(
            @PathVariable Long id, @Valid @RequestBody Usuario usuario, 
            Authentication authentication) {
        
        String login = authentication.getName();
        Usuario usuarioExistente = usuarioService.buscarPorId(id).orElse(null);
        
        if (usuarioExistente == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (!login.equals(usuarioExistente.getLogin()) && !authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(usuarioService.atualizar(id, usuario));
    }
    
    @PostMapping("/alterar-senha")
    public ResponseEntity<Void> alterarSenha(@RequestBody AlterarSenhaRequest request, 
                                           Authentication authentication) {
        String login = authentication.getName();
        usuarioService.alterarSenha(login, request.senhaAtual(), request.novaSenha());
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{id}/desativar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        usuarioService.desativar(id);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{id}/ativar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> ativar(@PathVariable Long id) {
        usuarioService.ativar(id);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        usuarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }
    
    public record AlterarSenhaRequest(String senhaAtual, String novaSenha) {}
}
