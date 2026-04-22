package com.hair.service;

import com.hair.dto.ServicoDTO;
import com.hair.exception.ServicoNotFoundException;
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
    
    public Servico salvar(ServicoDTO servicoDTO) {
        Servico servico = new Servico();
        servico.setIdServico(servicoDTO.getIdServico());
        servico.setNome(servicoDTO.getNome());
        servico.setPreco(servicoDTO.getPreco());
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
            throw new ServicoNotFoundException(id);
        }
    }
    
    public boolean existePorId(Long id) {
        return servicoRepository.existsById(id);
    }

    public Servico atualizar(Long id, ServicoDTO servicoDTO) {
        Servico servico = buscarPorId(id)
            .orElseThrow(() -> new ServicoNotFoundException(id));
        
        servico.setIdServico(servicoDTO.getIdServico());
        servico.setNome(servicoDTO.getNome());
        servico.setPreco(servicoDTO.getPreco());
        servico.setDataUltimaAlteracao(LocalDateTime.now());
        
        return servicoRepository.save(servico);
    }
}
