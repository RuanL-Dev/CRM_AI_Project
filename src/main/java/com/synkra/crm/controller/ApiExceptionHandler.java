package com.synkra.crm.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> notFound(NoSuchElementException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> validation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(err -> {
                String defaultMessage = err.getDefaultMessage();
                if (defaultMessage != null && defaultMessage.startsWith(err.getField() + ":")) {
                    return defaultMessage;
                }
                return err.getField() + ": " + defaultMessage;
            })
            .orElse("Requisição inválida");
        return Map.of("error", message);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> dataIntegrity(DataIntegrityViolationException ex) {
        String message = "Não foi possível salvar o registro com os dados informados";
        String causeMessage = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : "";

        if (causeMessage != null && causeMessage.toLowerCase().contains("email")) {
            message = "Já existe um contato cadastrado com este e-mail";
        }

        return Map.of("error", message);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public Map<String, String> unsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        return Map.of("error", "Formato de envio inválido. Atualize a página e tente novamente.");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> generic(Exception ex) {
        LOGGER.error("Unhandled application error", ex);
        return Map.of("error", "Ocorreu um erro interno inesperado");
    }
}
