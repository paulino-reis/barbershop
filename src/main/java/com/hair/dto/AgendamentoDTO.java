package com.hair.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AgendamentoDTO {
    
    private Long id;
    
    @NotNull(message = "Profissional é obrigatório")
    private Long profissionalId;
    
    @NotNull(message = "Serviço é obrigatório")
    private Long servicoId;
    
    @NotNull(message = "Data de agendamento é obrigatória")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataAgendamento;
    
    @NotNull(message = "Horário é obrigatório")
    private String horarioAgendado;
    
    private String status;
}
