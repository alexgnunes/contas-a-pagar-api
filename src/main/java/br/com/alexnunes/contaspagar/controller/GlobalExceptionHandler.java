package br.com.alexnunes.contaspagar.controller;

import br.com.alexnunes.contaspagar.application.auth.CredenciaisInvalidasException;
import br.com.alexnunes.contaspagar.domain.conta.exception.ContaNaoEncontradaException;
import br.com.alexnunes.contaspagar.domain.conta.exception.IntervaloDataInvalidoException;
import br.com.alexnunes.contaspagar.domain.conta.exception.SituacaoInvalidaException;
import br.com.alexnunes.contaspagar.domain.conta.exception.ValorInvalidoException;
import br.com.alexnunes.contaspagar.domain.fornecedor.exception.FornecedorComContasVinculadasException;
import br.com.alexnunes.contaspagar.domain.fornecedor.exception.FornecedorNaoEncontradoException;
import br.com.alexnunes.contaspagar.domain.importacao.exception.ArmazenamentoArquivoException;
import br.com.alexnunes.contaspagar.domain.importacao.exception.ArquivoVazioException;
import br.com.alexnunes.contaspagar.domain.importacao.exception.ImportacaoNaoEncontradaException;
import br.com.alexnunes.contaspagar.infrastructure.web.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

    @ExceptionHandler(ContaNaoEncontradaException.class)
    public ResponseEntity<ErrorResponse> handleContaNaoEncontrada(ContaNaoEncontradaException ex,
                                                                   HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ValorInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleValorInvalido(ValorInvalidoException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(SituacaoInvalidaException.class)
    public ResponseEntity<ErrorResponse> handleSituacaoInvalida(SituacaoInvalidaException ex,
                                                                  HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(IntervaloDataInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleIntervaloDataInvalido(IntervaloDataInvalidoException ex,
                                                                      HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleParametroObrigatorioAusente(MissingServletRequestParameterException ex,
                                                                            HttpServletRequest request) {
        String mensagem = String.format("Parâmetro obrigatório ausente: %s", ex.getParameterName());
        return build(HttpStatus.BAD_REQUEST, mensagem, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleParametroComTipoInvalido(MethodArgumentTypeMismatchException ex,
                                                                         HttpServletRequest request) {
        String mensagem = String.format("Parâmetro '%s' possui valor inválido: %s", ex.getName(), ex.getValue());
        return build(HttpStatus.BAD_REQUEST, mensagem, request);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleParteObrigatoriaAusente(MissingServletRequestPartException ex,
                                                                        HttpServletRequest request) {
        String mensagem = String.format("Parte obrigatória ausente: %s", ex.getRequestPartName());
        return build(HttpStatus.BAD_REQUEST, mensagem, request);
    }

    @ExceptionHandler(ArquivoVazioException.class)
    public ResponseEntity<ErrorResponse> handleArquivoVazio(ArquivoVazioException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(ArmazenamentoArquivoException.class)
    public ResponseEntity<ErrorResponse> handleArmazenamentoArquivo(ArmazenamentoArquivoException ex,
                                                                     HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    @ExceptionHandler(ImportacaoNaoEncontradaException.class)
    public ResponseEntity<ErrorResponse> handleImportacaoNaoEncontrada(ImportacaoNaoEncontradaException ex,
                                                                        HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleViolacaoIntegridade(DataIntegrityViolationException ex,
                                                                    HttpServletRequest request) {
        log.warn("Violação de integridade de dados", ex);
        return build(HttpStatus.CONFLICT, "Conflito ao persistir os dados da requisição", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleErroInesperado(Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado", request);
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
