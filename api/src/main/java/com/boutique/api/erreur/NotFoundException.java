package com.boutique.api.erreur;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}