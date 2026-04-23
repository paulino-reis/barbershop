package com.hair.controller;

import com.hair.security.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    
}
