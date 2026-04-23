package com.hair.service;

import com.hair.dto.UsuarioDTO;
import com.hair.exception.DuplicateResourceException;
import com.hair.exception.InvalidCredentialsException;
import com.hair.exception.UsuarioNotFoundException;
import com.hair.model.Usuario;
import com.hair.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService {
    
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    
    public Usuario salvar(Usuario usuario) {
        if (usuarioRepository.existsByLogin(usuario.getLogin())) {
            throw new DuplicateResourceException("Login", usuario.getLogin());
        }

        if (usuarioRepository.existsByTelefone(usuario.getTelefone())) {
            throw new DuplicateResourceException("Telefone", usuario.getTelefone());
        }

        definirRoleParaPrimeiroUsuario(usuario);

        usuario.setSenha(Objects.requireNonNull(passwordEncoder.encode(usuario.getSenha())));
        return usuarioRepository.save(usuario);
    }
    
    public Usuario salvar(UsuarioDTO usuarioDTO) {
        if (usuarioRepository.existsByLogin(usuarioDTO.getLogin())) {
            throw new DuplicateResourceException("Login", usuarioDTO.getLogin());
        }

        if (usuarioRepository.existsByTelefone(usuarioDTO.getTelefone())) {
            throw new DuplicateResourceException("Telefone", usuarioDTO.getTelefone());
        }

        if (usuarioDTO.getSenha() == null || usuarioDTO.getSenha().isEmpty()) {
            throw new IllegalArgumentException("Senha é obrigatória");
        }

        Usuario usuario = new Usuario();
        usuario.setNomeUsuario(usuarioDTO.getNomeUsuario());
        usuario.setTelefone(usuarioDTO.getTelefone());
        usuario.setEmail(usuarioDTO.getEmail() != null ? usuarioDTO.getEmail() : "");
        usuario.setEndereco(usuarioDTO.getEndereco());
        usuario.setValorPago(usuarioDTO.getValorPago());
        usuario.setLogin(usuarioDTO.getLogin());
        usuario.setSenha(usuarioDTO.getSenha());
        usuario.setRole(usuarioDTO.getRole());
        usuario.setAtivo(usuarioDTO.getAtivo());

        definirRoleParaPrimeiroUsuario(usuario);

        usuario.setSenha(Objects.requireNonNull(passwordEncoder.encode(usuario.getSenha())));
        return usuarioRepository.save(usuario);
    }
    
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }
    
    public Optional<Usuario> buscarPorLogin(String login) {
        return usuarioRepository.findByLoginAndAtivo(login);
    }
    
    public List<Usuario> buscarTodos() {
        return usuarioRepository.findAll();
    }
    
    public Usuario atualizar(Long id, Usuario usuario) {
        Usuario usuarioAtual = validarEObterUsuarioExistente(id, usuario.getLogin(), usuario.getTelefone());
        
        usuarioAtual.setNomeUsuario(usuario.getNomeUsuario());
        usuarioAtual.setTelefone(usuario.getTelefone());
        usuarioAtual.setLogin(usuario.getLogin());

        if (!usuario.getSenha().isEmpty()) {
            usuarioAtual.setSenha(Objects.requireNonNull(passwordEncoder.encode(usuario.getSenha())));
        }

        return usuarioRepository.save(usuarioAtual);
    }
    
    public Usuario atualizar(Long id, UsuarioDTO usuarioDTO) {
        Usuario usuarioAtual = validarEObterUsuarioExistente(id, usuarioDTO.getLogin(), usuarioDTO.getTelefone());
        
        usuarioAtual.setNomeUsuario(usuarioDTO.getNomeUsuario());
        usuarioAtual.setTelefone(usuarioDTO.getTelefone());
        usuarioAtual.setEmail(usuarioDTO.getEmail());
        usuarioAtual.setEndereco(usuarioDTO.getEndereco());
        usuarioAtual.setValorPago(usuarioDTO.getValorPago());
        usuarioAtual.setLogin(usuarioDTO.getLogin());
        usuarioAtual.setRole(usuarioDTO.getRole());
        usuarioAtual.setAtivo(usuarioDTO.getAtivo());
        
        if (usuarioDTO.getSenha() != null && !usuarioDTO.getSenha().isEmpty()) {
            usuarioAtual.setSenha(Objects.requireNonNull(passwordEncoder.encode(usuarioDTO.getSenha())));
        }
        
        return usuarioRepository.save(usuarioAtual);
    }
    
    private Usuario validarEObterUsuarioExistente(Long id, String novoLogin, String novoTelefone) {
        Optional<Usuario> usuarioExistente = usuarioRepository.findById(id);
        if (usuarioExistente.isEmpty()) {
            throw new UsuarioNotFoundException(id);
        }
        
        Usuario usuarioAtual = usuarioExistente.get();
        
        if (!usuarioAtual.getLogin().equals(novoLogin) && 
            usuarioRepository.existsByLogin(novoLogin)) {
            throw new DuplicateResourceException("Login", novoLogin);
        }
        
        if (!usuarioAtual.getTelefone().equals(novoTelefone) && 
            usuarioRepository.existsByTelefone(novoTelefone)) {
            throw new DuplicateResourceException("Telefone", novoTelefone);
        }
        
        return usuarioAtual;
    }
    
    public void alterarSenha(String login, String senhaAtual, String novaSenha) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByLogin(login);
        if (usuarioOpt.isEmpty()) {
            throw new UsuarioNotFoundException(login);
        }
        
        Usuario usuario = usuarioOpt.get();
        if (!passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
            throw new InvalidCredentialsException();
        }
        
        usuario.setSenha(Objects.requireNonNull(passwordEncoder.encode(novaSenha)));
        usuarioRepository.save(usuario);
    }
    
    public void desativar(Long id) {
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        if (usuario.isPresent()) {
            usuario.get().setAtivo(false);
            usuarioRepository.save(usuario.get());
        } else {
            throw new UsuarioNotFoundException(id);
        }
    }
    
    public void ativar(Long id) {
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        if (usuario.isPresent()) {
            usuario.get().setAtivo(true);
            usuarioRepository.save(usuario.get());
        } else {
            throw new UsuarioNotFoundException(id);
        }
    }
    
    public void deletar(Long id) {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
        } else {
            throw new UsuarioNotFoundException(id);
        }
    }

    private void definirRoleParaPrimeiroUsuario(Usuario usuario) {
        if (usuarioRepository.count() == 0) {
            usuario.setRole("ADMIN");
        }
    }

}
