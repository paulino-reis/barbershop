package com.hair.repository;

import com.hair.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {
    
    Optional<Servico> findByIdServico(String idServico);
    
    List<Servico> findByPrecoBetweenOrderByPreco(Double precoMin, Double precoMax);
    
    @Query("SELECT s FROM Servico s ORDER BY s.preco ASC")
    List<Servico> findAllOrderByPreco();
    
    boolean existsByIdServico(String idServico);
}
