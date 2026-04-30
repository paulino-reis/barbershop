package com.hair.controller;

import com.hair.security.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final AuthenticationService authenticationService;
    
    @PostMapping("/login")
    public ResponseEntity<AuthenticationService.AuthenticationResponse> login(
            @RequestBody AuthenticationService.AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }
    
    @PostMapping("/registrar")
    public ResponseEntity<AuthenticationService.AuthenticationResponse> registrar(
            @Valid @RequestBody AuthenticationService.RegistroRequest request) {
        return ResponseEntity.ok(authenticationService.registrar(request));
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @RequestBody Map<String, String> request) {
        String login = request.get("login");
        authenticationService.sendPasswordResetEmailByLogin(login);
        return ResponseEntity.ok(Map.of(
            "message", "Instruções para recuperação de senha foram enviadas para seu e-mail."
        ));
    }
    
    @PostMapping("/find-user-by-login")
    public ResponseEntity<Map<String, String>> findUserByLogin(
            @RequestBody Map<String, String> request) {
        String login = request.get("login");
        try {
            String email = authenticationService.findEmailByLogin(login);
            return ResponseEntity.ok(Map.of("email", email));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
        
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");
        
        try {
            authenticationService.resetPassword(token, newPassword);
            return ResponseEntity.ok(Map.of(
                "message", "Senha redefinida com sucesso."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
    
}
