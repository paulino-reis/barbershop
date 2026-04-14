package com.hair.service;

import com.hair.model.Usuario;
import com.hair.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService {
    
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    
    public Usuario salvar(Usuario usuario) {
        if (usuarioRepository.existsByLogin(usuario.getLogin())) {
            throw new RuntimeException("Login já existe: " + usuario.getLogin());
        }
        
        if (usuarioRepository.existsByTelefone(usuario.getTelefone())) {
            throw new RuntimeException("Telefone já cadastrado: " + usuario.getTelefone());
        }
        
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        return usuarioRepository.save(usuario);
    }
    
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }
    
    public Optional<Usuario> buscarPorLogin(String login) {
        return usuarioRepository.findByLoginAndAtivo(login);
    }
    
    public Optional<Usuario> buscarPorTelefone(String telefone) {
        return usuarioRepository.findByTelefone(telefone);
    }
    
    public List<Usuario> buscarTodos() {
        return usuarioRepository.findAll();
    }
    
    public Usuario atualizar(Long id, Usuario usuario) {
        Optional<Usuario> usuarioExistente = usuarioRepository.findById(id);
        if (usuarioExistente.isEmpty()) {
            throw new RuntimeException("Usuário não encontrado com ID: " + id);
        }
        
        Usuario usuarioAtual = usuarioExistente.get();
        
        if (!usuarioAtual.getLogin().equals(usuario.getLogin()) && 
            usuarioRepository.existsByLogin(usuario.getLogin())) {
            throw new RuntimeException("Login já existe: " + usuario.getLogin());
        }
        
        if (!usuarioAtual.getTelefone().equals(usuario.getTelefone()) && 
            usuarioRepository.existsByTelefone(usuario.getTelefone())) {
            throw new RuntimeException("Telefone já cadastrado: " + usuario.getTelefone());
        }
        
        usuarioAtual.setNomeUsuario(usuario.getNomeUsuario());
        usuarioAtual.setTelefone(usuario.getTelefone());
        usuarioAtual.setLogin(usuario.getLogin());
        
        if (usuario.getSenha() != null && !usuario.getSenha().isEmpty()) {
            usuarioAtual.setSenha(passwordEncoder.encode(usuario.getSenha()));
        }
        
        return usuarioRepository.save(usuarioAtual);
    }
    
    public void alterarSenha(String login, String senhaAtual, String novaSenha) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByLogin(login);
        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Usuário não encontrado: " + login);
        }
        
        Usuario usuario = usuarioOpt.get();
        if (!passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
            throw new RuntimeException("Senha atual incorreta");
        }
        
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }
    
    public void desativar(Long id) {
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        if (usuario.isPresent()) {
            usuario.get().setAtivo(false);
            usuarioRepository.save(usuario.get());
        } else {
            throw new RuntimeException("Usuário não encontrado com ID: " + id);
        }
    }
    
    public void ativar(Long id) {
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        if (usuario.isPresent()) {
            usuario.get().setAtivo(true);
            usuarioRepository.save(usuario.get());
        } else {
            throw new RuntimeException("Usuário não encontrado com ID: " + id);
        }
    }
    
    public void deletar(Long id) {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
        } else {
            throw new RuntimeException("Usuário não encontrado com ID: " + id);
        }
    }
    
    public boolean existePorLogin(String login) {
        return usuarioRepository.existsByLogin(login);
    }
    
    public boolean existePorId(Long id) {
        return usuarioRepository.existsById(id);
    }
}
