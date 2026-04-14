package com.hair.repository;

import com.hair.model.Agendamento;
import com.hair.model.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {
    
    List<Agendamento> findByProfissionalOrderByDataAgendamento(Profissional profissional);
    
    List<Agendamento> findByDataAgendamentoBetween(LocalDateTime inicio, LocalDateTime fim);
    
    @Query("SELECT a FROM Agendamento a WHERE a.profissional.id = :profissionalId " +
           "AND a.dataAgendamento = :data AND a.horarioAgendado = :horario")
    List<Agendamento> findByProfissionalAndDataAndHorario(@Param("profissionalId") Long profissionalId,
                                                          @Param("data") LocalDateTime data,
                                                          @Param("horario") String horario);
    
    @Query("SELECT a.horarioAgendado FROM Agendamento a WHERE " +
           "a.profissional.id = :profissionalId AND a.dataAgendamento = :data " +
           "AND a.status != 'CANCELADO'")
    List<String> findHorariosOcupadosByProfissionalAndData(@Param("profissionalId") Long profissionalId,
                                                           @Param("data") LocalDateTime data);
    
    @Query("SELECT a.horarioAgendado FROM Agendamento a WHERE " +
           "a.dataAgendamento = :data AND a.status != 'CANCELADO'")
    List<String> findHorariosOcupadosByData(@Param("data") LocalDateTime data);
    
    @Query("SELECT a FROM Agendamento a WHERE a.usuario.login = :login " +
           "ORDER BY a.dataAgendamento DESC")
    List<Agendamento> findByUsuarioLogin(@Param("login") String login);
    
    boolean existsByProfissionalIdAndDataAgendamentoAndHorarioAgendado(Long profissionalId, 
                                                                      LocalDateTime data, 
                                                                      String horario);
}
