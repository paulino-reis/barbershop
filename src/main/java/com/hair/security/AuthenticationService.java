package com.hair.security;

import com.hair.dto.UsuarioDTO;
import com.hair.model.Usuario;
import com.hair.repository.UsuarioRepository;
import com.hair.service.UsuarioService;
import lombok.RequiredArgsConstructor;
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
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioService usuarioService;
    
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
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
        if (request.senha().isBlank()) {
            throw new IllegalArgumentException("Senha não pode ser vazia");
        }

        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setNomeUsuario(request.nomeUsuario());
        usuarioDTO.setLogin(request.login());
        usuarioDTO.setSenha(request.senha());
        usuarioDTO.setTelefone(request.telefone());
        usuarioDTO.setEmail(request.email());
        usuarioDTO.setRole("USER");
        usuarioDTO.setAtivo(true);

        Usuario usuarioSalvo = usuarioService.salvar(usuarioDTO);

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
    public record AuthenticationResponse(String token, Long id, String login, String nomeUsuario, String role) {}
    public record RegistroRequest(String nomeUsuario, String login, String senha, String telefone, String email) {}
}
