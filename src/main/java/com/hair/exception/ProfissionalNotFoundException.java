package com.hair.exception;

public class ProfissionalNotFoundException extends RuntimeException {
    
    public ProfissionalNotFoundException(Long id) {
        super("Profissional não encontrado com ID: " + id);
    }

}
