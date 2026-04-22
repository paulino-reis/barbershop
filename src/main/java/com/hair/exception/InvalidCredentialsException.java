package com.hair.exception;

public class InvalidCredentialsException extends RuntimeException {
    
    public InvalidCredentialsException() {
        super("Senha atual incorreta");
    }

}
