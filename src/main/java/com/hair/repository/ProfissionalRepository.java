package com.hair.repository;

import com.hair.model.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {
    
    Optional<Profissional> findByTelefone(String telefone);
    
    List<Profissional> findByNomeContainingIgnoreCase(String nome);
    
    List<Profissional> findByCidadeAndUf(String cidade, String uf);
    
    @Query("SELECT p FROM Profissional p WHERE p.id IN " +
           "(SELECT DISTINCT a.profissional.id FROM Agendamento a WHERE " +
           "a.dataAgendamento = :data AND a.horarioAgendado = :horario)")
    List<Profissional> findProfissionaisOcupados(@Param("data") java.time.LocalDateTime data, 
                                                @Param("horario") String horario);
    
    @Query("SELECT p FROM Profissional p WHERE p.id NOT IN " +
           "(SELECT DISTINCT a.profissional.id FROM Agendamento a WHERE " +
           "a.dataAgendamento = :data AND a.horarioAgendado = :horario)")
    List<Profissional> findProfissionaisDisponiveis(@Param("data") java.time.LocalDateTime data, 
                                                   @Param("horario") String horario);
}
