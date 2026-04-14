package com.hair.service;

import com.hair.model.Servico;
import com.hair.repository.ServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ServicoService {
    
    private final ServicoRepository servicoRepository;
    
    public Servico salvar(Servico servico) {
        servico.setDataUltimaAlteracao(LocalDateTime.now());
        return servicoRepository.save(servico);
    }
    
    public Optional<Servico> buscarPorId(Long id) {
        return servicoRepository.findById(id);
    }
    
    public Optional<Servico> buscarPorIdServico(String idServico) {
        return servicoRepository.findByIdServico(idServico);
    }
    
    public List<Servico> buscarTodos() {
        return servicoRepository.findAllOrderByPreco();
    }
    
    public List<Servico> buscarPorFaixaPreco(Double precoMin, Double precoMax) {
        return servicoRepository.findByPrecoBetweenOrderByPreco(precoMin, precoMax);
    }
    
    public void deletar(Long id) {
        if (servicoRepository.existsById(id)) {
            servicoRepository.deleteById(id);
        } else {
            throw new RuntimeException("Serviço não encontrado com ID: " + id);
        }
    }
    
    public boolean existePorId(Long id) {
        return servicoRepository.existsById(id);
    }
    
    public boolean existePorIdServico(String idServico) {
        return servicoRepository.existsByIdServico(idServico);
    }
}
