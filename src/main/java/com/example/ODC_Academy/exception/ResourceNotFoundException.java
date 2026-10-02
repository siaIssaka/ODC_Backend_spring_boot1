package com.example.ODC_Academy.exception;

/**
 * Levée lorsqu'une ressource demandée (utilisateur, cours, leçon...) n'existe pas.
 * Traduite automatiquement en réponse HTTP 404 par le {@link GlobalExceptionHandler}.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String resource, Object identifier) {
        return new ResourceNotFoundException(resource + " introuvable avec l'identifiant : " + identifier);
    }
}
