package com.hair.exception;

public class AgendamentoNotFoundException extends RuntimeException {
    
    public AgendamentoNotFoundException(Long id) {
        super("Agendamento não encontrado com ID: " + id);
    }

}
