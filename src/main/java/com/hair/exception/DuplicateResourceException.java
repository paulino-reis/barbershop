package com.hair.exception;

public class DuplicateResourceException extends RuntimeException {
    
    public DuplicateResourceException(String resource, String value) {
        super(resource + " já existe: " + value);
    }
}
