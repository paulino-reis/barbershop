package com.hair.security;

import com.hair.dto.UsuarioDTO;
import com.hair.model.Usuario;
import com.hair.model.PasswordResetToken;
import com.hair.repository.UsuarioRepository;
import com.hair.repository.PasswordResetTokenRepository;
import com.hair.service.ResendEmailService;
import com.hair.service.TenantContext;
import com.hair.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioService usuarioService;
    private final ResendEmailService resendEmailService;
    
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.login(),
                request.senha()
            )
        );
        
        Integer currentTenantId = TenantContext.getCurrentTenantId();
        Usuario usuario;
        
        if (currentTenantId != null) {
            usuario = usuarioRepository.findByLoginAndTenantId(request.login(), currentTenantId)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado para este tenant"));
        } else {
            usuario = usuarioRepository.findByLogin(request.login())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        }
        
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
    
    @Transactional
    public void sendPasswordResetEmail(String email) {
        log.info("Enviando e-mail de recuperação de senha para: {}", email);
        
        // Buscar usuário pelo e-mail
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        
        if (usuarioOpt.isEmpty()) {
            // Por segurança, não informamos que o e-mail não existe
            log.warn("Tentativa de recuperação de senha para e-mail não encontrado: {}", email);
            return;
        }
        
        Usuario usuario = usuarioOpt.get();
        
        // Invalidar tokens anteriores deste usuário
        tokenRepository.invalidateTokensByUsuario(usuario.getId().intValue(), java.time.LocalDateTime.now());
        
        // Gerar token de reset
        String resetToken = UUID.randomUUID().toString();
        
        // Criar e salvar token no banco
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setToken(resetToken);
        passwordResetToken.setUsuario(usuario);
        passwordResetToken.setExpiresAt(java.time.LocalDateTime.now().plusHours(1)); // Expira em 1 hora
        
        tokenRepository.save(passwordResetToken);
        
        try {
            // Enviar e-mail com o token usando Resend
            resendEmailService.sendPasswordResetEmail(
                usuario.getEmail(), 
                resetToken, 
                usuario.getNomeUsuario()
            );
            
            log.info("E-mail de recuperação enviado com sucesso para: {}", email);
            
        } catch (Exception e) {
            log.error("Erro ao enviar e-mail de recuperação para: {}", email, e);
            throw new RuntimeException("Falha ao enviar e-mail de recuperação", e);
        }
        
        log.info("Token de reset gerado para usuário {} ({}): {}", 
                usuario.getNomeUsuario(), usuario.getEmail(), resetToken);
    }
    
    @Transactional
    public void sendPasswordResetEmailByLogin(String login) {
        log.info("Enviando e-mail de recuperação de senha para o login: {}", login);
        
        // Obter tenant_id do contexto atual
        Integer currentTenantId = TenantContext.getCurrentTenantId();
        
        Optional<Usuario> usuarioOpt;
        
        if (currentTenantId == null) {
            log.warn("TenantContext não encontrado, usando busca global para login: {}", login);
            // Fallback para busca sem tenant se não houver contexto
            usuarioOpt = usuarioRepository.findByLogin(login);
        } else {
            // Busca específica do tenant
            usuarioOpt = usuarioRepository.findByLoginAndTenantId(login, currentTenantId);
            log.info("Buscando usuário {} no tenant {}", login, currentTenantId);
        }
        
        if (usuarioOpt.isEmpty()) {
            // Por segurança, não informamos que o login não existe
            log.warn("Tentativa de recuperação de senha para login não encontrado: {} (tenant: {})", login, currentTenantId);
            return;
        }
        
        Usuario usuario = usuarioOpt.get();
        
        // Invalidar tokens anteriores deste usuário
        tokenRepository.invalidateTokensByUsuario(usuario.getId().intValue(), java.time.LocalDateTime.now());
        
        // Gerar token de reset
        String resetToken = UUID.randomUUID().toString();
        
        // Criar e salvar token no banco
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setToken(resetToken);
        passwordResetToken.setUsuario(usuario);
        passwordResetToken.setExpiresAt(java.time.LocalDateTime.now().plusHours(1)); // Expira em 1 hora
        
        tokenRepository.save(passwordResetToken);
        
        try {
            // Enviar e-mail com o token usando Resend
            resendEmailService.sendPasswordResetEmail(
                usuario.getEmail(), 
                resetToken, 
                usuario.getNomeUsuario()
            );
            
            log.info("E-mail de recuperação enviado com sucesso para o login: {} | Email: {} | Tenant: {}", 
                    login, usuario.getEmail(), currentTenantId);
            
        } catch (Exception e) {
            log.error("Erro ao enviar e-mail de recuperação para o login: {} | Email: {} | Tenant: {}", 
                    login, usuario.getEmail(), currentTenantId, e);
            throw new RuntimeException("Falha ao enviar e-mail de recuperação", e);
        }
        
        log.info("Token de reset gerado para usuário {} ({}) | Tenant: {}", 
                usuario.getNomeUsuario(), usuario.getEmail(), currentTenantId);
    }
    
    public String findEmailByLogin(String login) {
        // Obter tenant_id do contexto atual
        Integer currentTenantId = TenantContext.getCurrentTenantId();
        
        log.info("Buscando email para login: {} | TenantContext: {}", login, currentTenantId);
        
        Optional<Usuario> usuarioOpt;
        
        if (currentTenantId == null) {
            log.warn("TenantContext não encontrado, usando busca global para login: {}", login);
            // Fallback para busca sem tenant se não houver contexto
            usuarioOpt = usuarioRepository.findByLogin(login);
        } else {
            // Busca específica do tenant
            usuarioOpt = usuarioRepository.findByLoginAndTenantId(login, currentTenantId);
            log.info("Busca específica do tenant {} para login: {}", currentTenantId, login);
        }
        
        if (usuarioOpt.isEmpty()) {
            log.warn("Usuário não encontrado: {} | Tenant: {}", login, currentTenantId);
            throw new RuntimeException("Usuário não encontrado");
        }
        
        Usuario usuario = usuarioOpt.get();
        
        if (usuario.getEmail().trim().isEmpty()) {
            log.warn("Usuário encontrado mas sem e-mail: {} | Email: {}", login, usuario.getEmail());
            throw new RuntimeException("Usuário não possui e-mail cadastrado");
        }
        
        log.info("Email encontrado para login {} (tenant {}): {}", login, currentTenantId, usuario.getEmail());
        return usuario.getEmail();
    }
        
    @Transactional
    public void resetPassword(@Nullable String token, String newPassword) {
        log.info("Tentando redefinir senha com token: {}", token);
        
        if (token == null || token.trim().isEmpty()) {
            throw new RuntimeException("Token inválido");
        }
        
        // Buscar token no banco
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByTokenAndUsedFalse(token);
        
        if (tokenOpt.isEmpty()) {
            throw new RuntimeException("Token inválido ou não encontrado");
        }
        
        PasswordResetToken resetToken = tokenOpt.get();
        
        // Verificar se o token não expirou
        if (resetToken.isExpired()) {
            throw new RuntimeException("Token expirado. Por favor, solicite uma nova recuperação de senha.");
        }
        
        // Verificar se o token já foi usado
        if (resetToken.getUsed()) {
            throw new RuntimeException("Token já foi utilizado. Por favor, solicite uma nova recuperação de senha.");
        }
        
        Usuario usuario = resetToken.getUsuario();
        
        // Atualizar a senha do usuário
        String hashedPassword = passwordEncoder.encode(newPassword);
        usuario.setSenha(hashedPassword);
        usuarioRepository.save(usuario);
        
        // Marcar token como usado
        resetToken.markAsUsed();
        tokenRepository.save(resetToken);
        
        // Invalidar outros tokens deste usuário
        tokenRepository.invalidateTokensByUsuario(usuario.getId().intValue(), java.time.LocalDateTime.now());
        
        log.info("Senha redefinida com sucesso para o usuário: {} (ID: {})", 
                usuario.getNomeUsuario(), usuario.getId());
    }
    
    public record AuthenticationRequest(String login, String senha) {}
    public record AuthenticationResponse(String token, Long id, String login, String nomeUsuario, String role) {}
    public record RegistroRequest(String nomeUsuario, String login, String senha, String telefone, String email) {}
}
