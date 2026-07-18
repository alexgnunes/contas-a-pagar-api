package br.com.alexnunes.contaspagar.controller;

import br.com.alexnunes.contaspagar.application.auth.CredenciaisInvalidasException;
import br.com.alexnunes.contaspagar.domain.fornecedor.exception.FornecedorComContasVinculadasException;
import br.com.alexnunes.contaspagar.domain.fornecedor.exception.FornecedorNaoEncontradoException;
import br.com.alexnunes.contaspagar.infrastructure.web.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErrorResponse> handleCredenciaisInvalidas(CredenciaisInvalidasException ex,
                                                                     HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(FornecedorNaoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleFornecedorNaoEncontrado(FornecedorNaoEncontradoException ex,
                                                                        HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(FornecedorComContasVinculadasException.class)
    public ResponseEntity<ErrorResponse> handleFornecedorComContasVinculadas(FornecedorComContasVinculadasException ex,
                                                                              HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI());

        return ResponseEntity.status(status).body(body);
    }

}
