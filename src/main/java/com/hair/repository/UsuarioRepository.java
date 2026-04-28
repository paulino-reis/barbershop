package com.hair.repository;

import com.hair.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    Optional<Usuario> findByLogin(String login);
    
    boolean existsByLogin(String login);
    
    boolean existsByTelefone(String telefone);
    
    @Query("SELECT u FROM Usuario u WHERE u.login = :login AND u.ativo = true")
    Optional<Usuario> findByLoginAndAtivo(@Param("login") String login);
    
    @Query("SELECT u FROM Usuario u WHERE u.login = :login AND u.tenantId = :tenantId")
    Optional<Usuario> findByLoginAndTenantId(@Param("login") String login, @Param("tenantId") Integer tenantId);
}
