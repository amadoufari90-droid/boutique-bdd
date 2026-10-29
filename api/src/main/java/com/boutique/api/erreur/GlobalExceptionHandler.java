package com.boutique.api.erreur;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> notFound(NotFoundException e) {
        return reponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(RequeteInvalideException.class)
    public ResponseEntity<Map<String, Object>> requeteInvalide(RequeteInvalideException e) {
        return reponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> corpsIllisible(HttpMessageNotReadableException e) {
        return reponse(HttpStatus.BAD_REQUEST, "Corps de requête illisible ou mal formé");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> invalide(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + " : " + f.getDefaultMessage())
                .collect(Collectors.joining(" ; "));
        return reponse(HttpStatus.BAD_REQUEST, detail);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> conflit(DataIntegrityViolationException e) {
        return reponse(HttpStatus.CONFLICT,
                "Opération refusée par la base (stock insuffisant, référence inexistante ou donnée encore utilisée)");
    }

    private ResponseEntity<Map<String, Object>> reponse(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(Map.of("status", status.value(), "erreur", message));
    }
}