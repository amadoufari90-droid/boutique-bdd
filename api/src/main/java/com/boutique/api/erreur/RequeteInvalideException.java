package com.boutique.api.erreur;

public class RequeteInvalideException extends RuntimeException {
    public RequeteInvalideException(String message) {
        super(message);
    }
}