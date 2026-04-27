package com.hair.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "servicos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Servico extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "id_servico", unique = true)
    private String idServico;
    
    @Column(nullable = false)
    private String nome;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;
    
    @Column(name = "data_ultima_alteracao")
    private LocalDateTime dataUltimaAlteracao;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
