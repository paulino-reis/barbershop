package com.hair.service;

import com.hair.dto.ProfissionalDTO;
import com.hair.exception.ProfissionalNotFoundException;
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
        return profissionalRepository.save(profissional);
    }
    
    public Profissional salvar(ProfissionalDTO profissionalDTO) {
        Profissional profissional = new Profissional();
        profissional.setNome(profissionalDTO.getNome());
        profissional.setTelefone(profissionalDTO.getTelefone());
        profissional.setFoto(profissionalDTO.getFoto());
        profissional.setDataInicioEmpresa(profissionalDTO.getDataInicioEmpresa() != null
            ? profissionalDTO.getDataInicioEmpresa()
            : LocalDateTime.now());
        copiarCamposDTOParaEntidade(profissionalDTO, profissional);
        return profissionalRepository.save(profissional);
    }
    
    public Optional<Profissional> buscarPorId(Long id) {
        return profissionalRepository.findById(id);
    }
    
    public List<Profissional> buscarTodos() {
        return profissionalRepository.findAll();
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
            throw new ProfissionalNotFoundException(id);
        }
    }
    
    public boolean existePorId(Long id) {
        return profissionalRepository.existsById(id);
    }
    
    public Profissional atualizar(Long id, ProfissionalDTO profissionalDTO) {
        Profissional profissional = buscarPorId(id)
            .orElseThrow(() -> new ProfissionalNotFoundException(id));

        profissional.setNome(profissionalDTO.getNome());
        profissional.setTelefone(profissionalDTO.getTelefone());
        profissional.setFoto(profissionalDTO.getFoto());
        profissional.setDataInicioEmpresa(profissionalDTO.getDataInicioEmpresa());
        copiarCamposDTOParaEntidade(profissionalDTO, profissional);

        return profissionalRepository.save(profissional);
    }

    private void copiarCamposDTOParaEntidade(ProfissionalDTO dto, Profissional profissional) {
        profissional.setCep(dto.getCep());
        profissional.setEndereco(dto.getEndereco());
        profissional.setComplemento(dto.getComplemento());
        profissional.setBairro(dto.getBairro());
        profissional.setCidade(dto.getCidade());
        profissional.setUf(dto.getUf());
        profissional.setHoraInicio(dto.getHoraInicio());
        profissional.setHoraFim(dto.getHoraFim());
        profissional.setDiasDisponiveis(dto.getDiasDisponiveis());
    }
}
