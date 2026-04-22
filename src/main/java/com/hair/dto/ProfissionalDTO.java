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
    
    private String foto;
    
    @NotNull(message = "Data de início na empresa é obrigatória")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataInicioEmpresa;
    
    @NotBlank(message = "CEP é obrigatório")
    private String cep;
    
    @NotBlank(message = "Endereço é obrigatório")
    private String endereco;
    
    private String complemento;
    
    @NotBlank(message = "Bairro é obrigatório")
    private String bairro;
    
    @NotBlank(message = "Cidade é obrigatória")
    private String cidade;
    
    @NotBlank(message = "UF é obrigatória")
    private String uf;
    
    private String horaInicio;
    
    private String horaFim;
    
    private String diasDisponiveis;
}
