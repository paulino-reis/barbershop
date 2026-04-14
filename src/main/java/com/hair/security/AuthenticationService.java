package com.hair.security;

import com.hair.model.Usuario;
import com.hair.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final ObjectProvider<AuthenticationManager> authenticationManagerProvider;
    private final ObjectProvider<PasswordEncoder> passwordEncoderProvider;
    
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        AuthenticationManager authenticationManager = authenticationManagerProvider.getObject();
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.login(),
                request.senha()
            )
        );
        
        Usuario usuario = usuarioRepository.findByLogin(request.login())
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        
        String jwtToken = jwtService.generateToken(usuario);
        
        return new AuthenticationResponse(
            jwtToken,
            usuario.getId(),
            usuario.getLogin(),
            usuario.getNomeUsuario(),
            usuario.getRole()
        );
    }
    
    public AuthenticationResponse registrar(RegistroRequest request) {
        Usuario usuario = new Usuario();
        usuario.setNomeUsuario(request.nomeUsuario());
        usuario.setLogin(request.login());
        usuario.setSenha(passwordEncoderProvider.getObject().encode(request.senha()));
        usuario.setTelefone(request.telefone());
        usuario.setRole("USER");
        
        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        
        String jwtToken = jwtService.generateToken(usuarioSalvo);
        
        return new AuthenticationResponse(
            jwtToken,
            usuarioSalvo.getId(),
            usuarioSalvo.getLogin(),
            usuarioSalvo.getNomeUsuario(),
            usuarioSalvo.getRole()
        );
    }
    
    public record AuthenticationRequest(String login, String senha) {}
    public record AuthenticationResponse(String token, Long id, String login, String nome, String role) {}
    public record RegistroRequest(String nomeUsuario, String login, String senha, String telefone) {}
}
