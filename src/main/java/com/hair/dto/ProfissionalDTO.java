package com.hair.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProfissionalDTO {
    
    private Long id;
    
    @NotBlank(message = "Nome é obrigatório")
    private String nome;
    
    @NotBlank(message = "Telefone é obrigatório")
    private String telefone;

    private String email;

    private String foto;
    
    @NotNull(message = "Data de início na empresa é obrigatória")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataInicioEmpresa;

    private String cep;

    private String endereco;

    private String complemento;

    private String bairro;

    private String cidade;

    private String uf;
    
    private String horaInicio;
    
    private String horaFim;
    
    private String diasDisponiveis;
}
