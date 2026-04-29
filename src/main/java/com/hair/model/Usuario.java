package com.hair.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario extends BaseEntity implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "nome_usuario", nullable = false)
    private String nomeUsuario;
    
    @Column(nullable = false)
    private String telefone;
    
    @Column(nullable = true)
    private String email;
    
    @Column(name = "endereco")
    private String endereco;
    
    @Column(nullable = false, unique = true)
    private String login;
    
    @Column(nullable = false)
    private String senha;
    
    @Column(nullable = false)
    private String role = "USER";
    
    @Column(nullable = false)
    private Boolean ativo = true;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }
    
    @Override
    public String getUsername() {
        return login;
    }
    
    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public boolean isEnabled() {
        return ativo;
    }
}
