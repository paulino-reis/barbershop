package com.hair.controller;

import com.hair.dto.UsuarioDTO;
import com.hair.model.Usuario;
import com.hair.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

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
        Usuario usuario = usuarioService.buscarPorId(id).orElse(null);
        
        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (temAcesso(authentication, usuario.getLogin())) {
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
    public ResponseEntity<Usuario> cadastrar(@Valid @RequestBody UsuarioDTO usuarioDTO) {
        return ResponseEntity.ok(usuarioService.salvar(usuarioDTO));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> atualizar(
            @PathVariable Long id, @Valid @RequestBody UsuarioDTO usuarioDTO, 
            Authentication authentication) {
        
        Usuario usuarioExistente = usuarioService.buscarPorId(id).orElse(null);
        
        if (usuarioExistente == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (temAcesso(authentication, usuarioExistente.getLogin())) {
            return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(usuarioService.atualizar(id, usuarioDTO));
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
    
    private boolean temAcesso(Authentication authentication, String loginUsuario) {
        String loginAutenticado = authentication.getName();
        return !loginAutenticado.equals(loginUsuario) &&
                authentication.getAuthorities().stream()
                        .noneMatch(auth -> Objects.equals(auth.getAuthority(), "ROLE_ADMIN"));
    }
    
    public record AlterarSenhaRequest(String senhaAtual, String novaSenha) {}
}
