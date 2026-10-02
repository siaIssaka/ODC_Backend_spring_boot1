package com.example.ODC_Academy.exception;

/**
 * Levée lors d'une tentative de création d'une ressource déjà existante
 * (ex : email déjà utilisé, inscription déjà effectuée).
 * Traduite automatiquement en réponse HTTP 409 (Conflict).
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
