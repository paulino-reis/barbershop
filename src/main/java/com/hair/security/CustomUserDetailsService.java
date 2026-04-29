package com.hair.security;

import com.hair.repository.UsuarioRepository;
import com.hair.service.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UsuarioRepository usuarioRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Integer currentTenantId = TenantContext.getCurrentTenantId();
        
        if (currentTenantId != null) {
            return usuarioRepository.findByLoginAndTenantId(username, currentTenantId)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado para este tenant: " + username));
        }
        
        return usuarioRepository.findByLogin(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
    }
}
