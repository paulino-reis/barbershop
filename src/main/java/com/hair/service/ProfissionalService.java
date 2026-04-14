package com.hair.service;

import com.hair.model.Profissional;
import com.hair.repository.ProfissionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfissionalService {
    
    private final ProfissionalRepository profissionalRepository;
    
    public Profissional salvar(Profissional profissional) {
        if (profissional.getDataInicioEmpresa() == null) {
            profissional.setDataInicioEmpresa(LocalDateTime.now());
        }
        return profissionalRepository.save(profissional);
    }
    
    public Optional<Profissional> buscarPorId(Long id) {
        return profissionalRepository.findById(id);
    }
    
    public List<Profissional> buscarTodos() {
        return profissionalRepository.findAll();
    }
    
    public Optional<Profissional> buscarPorTelefone(String telefone) {
        return profissionalRepository.findByTelefone(telefone);
    }
    
    public List<Profissional> buscarPorNome(String nome) {
        return profissionalRepository.findByNomeContainingIgnoreCase(nome);
    }
    
    public List<Profissional> buscarPorCidadeUf(String cidade, String uf) {
        return profissionalRepository.findByCidadeAndUf(cidade, uf);
    }
    
    public List<Profissional> buscarDisponiveis(LocalDateTime data, String horario) {
        return profissionalRepository.findProfissionaisDisponiveis(data, horario);
    }
    
    public void deletar(Long id) {
        if (profissionalRepository.existsById(id)) {
            profissionalRepository.deleteById(id);
        } else {
            throw new RuntimeException("Profissional não encontrado com ID: " + id);
        }
    }
    
    public boolean existePorId(Long id) {
        return profissionalRepository.existsById(id);
    }
}
