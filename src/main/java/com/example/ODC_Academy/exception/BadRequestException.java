package com.example.ODC_Academy.exception;

/**
 * Levée lorsque la requête du client est syntaxiquement correcte mais
 * sémantiquement invalide au regard des règles métier.
 * Traduite automatiquement en réponse HTTP 400.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);

        
    }
}
