package com.hair.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UsuarioDTO {
    
    private Long id;
    
    @NotBlank(message = "Nome do usuário é obrigatório")
    private String nomeUsuario;
    
    @NotBlank(message = "Telefone é obrigatório")
    private String telefone;
    
    private String email;
    
    private String endereco;
    
    private Long servicoId;
    
    private Long profissionalId;
    
    private BigDecimal valorPago;
    
    @NotBlank(message = "Login é obrigatório")
    private String login;
    
    @NotBlank(message = "Senha é obrigatória")
    private String senha;
    
    private String role;
    
    private Boolean ativo;
}
