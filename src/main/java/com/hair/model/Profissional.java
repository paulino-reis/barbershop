package com.hair.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "profissionais")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Profissional extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nome;
    
    @Column(nullable = false)
    private String telefone;

    private String email;

    @Column(name = "foto_url")
    private String foto;
    
    @Column(name = "data_inicio_empresa")
    private LocalDateTime dataInicioEmpresa;
    
    @Column(nullable = false)
    private String cep;
    
    @Column(nullable = false)
    private String endereco;
    
    private String complemento;
    
    @Column(nullable = false)
    private String bairro;
    
    @Column(nullable = false)
    private String cidade;
    
    @Column(nullable = false, length = 2)
    private String uf;
    
    @Column(name = "hora_inicio")
    private String horaInicio;
    
    @Column(name = "hora_fim")
    private String horaFim;
    
    @Column(name = "dias_disponiveis")
    private String diasDisponiveis;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
