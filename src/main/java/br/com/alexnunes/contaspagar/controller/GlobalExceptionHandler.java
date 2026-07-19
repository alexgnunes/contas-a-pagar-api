package br.com.alexnunes.contaspagar.controller;

import br.com.alexnunes.contaspagar.application.auth.CredenciaisInvalidasException;
import br.com.alexnunes.contaspagar.domain.conta.exception.ContaNaoEncontradaException;
import br.com.alexnunes.contaspagar.domain.conta.exception.DataPagamentoInvalidaException;
import br.com.alexnunes.contaspagar.domain.conta.exception.IntervaloDataInvalidoException;
import br.com.alexnunes.contaspagar.domain.conta.exception.SituacaoInvalidaException;
import br.com.alexnunes.contaspagar.domain.conta.exception.ValorInvalidoException;
import br.com.alexnunes.contaspagar.domain.fornecedor.exception.FornecedorComContasVinculadasException;
import br.com.alexnunes.contaspagar.domain.fornecedor.exception.FornecedorNaoEncontradoException;
import br.com.alexnunes.contaspagar.domain.importacao.exception.ArmazenamentoArquivoException;
import br.com.alexnunes.contaspagar.domain.importacao.exception.ArquivoVazioException;
import br.com.alexnunes.contaspagar.domain.importacao.exception.ImportacaoNaoEncontradaException;
import br.com.alexnunes.contaspagar.infrastructure.web.ErrorResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.stream.Collectors;

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

    @ExceptionHandler(DataPagamentoInvalidaException.class)
    public ResponseEntity<ErrorResponse> handleDataPagamentoInvalida(DataPagamentoInvalidaException ex,
                                                                       HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(IntervaloDataInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleIntervaloDataInvalido(IntervaloDataInvalidoException ex,
                                                                      HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleArgumentoInvalido(MethodArgumentNotValidException ex,
                                                                   HttpServletRequest request) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> String.format("%s: %s", erro.getField(), erro.getDefaultMessage()))
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, mensagem, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleCorpoIlegivel(HttpMessageNotReadableException ex,
                                                               HttpServletRequest request) {
        Throwable causa = ex.getMostSpecificCause();
        String mensagem = causa instanceof InvalidFormatException invalidFormat
                ? String.format("Valor inválido para o campo '%s': %s", nomeDoCampo(invalidFormat),
                        invalidFormat.getValue())
                : "Corpo da requisição inválido ou malformado";
        return build(HttpStatus.BAD_REQUEST, mensagem, request);
    }

    private String nomeDoCampo(InvalidFormatException ex) {
        return ex.getPath().isEmpty() ? "desconhecido" : ex.getPath().get(ex.getPath().size() - 1).getFieldName();
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErrorResponse> handlePropriedadeInvalida(PropertyReferenceException ex,
                                                                     HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(org.springframework.dao.InvalidDataAccessApiUsageException.class)
    public ResponseEntity<ErrorResponse> handleUsoInvalidoDeApiDeAcessoADados(
            org.springframework.dao.InvalidDataAccessApiUsageException ex, HttpServletRequest request) {
        if (causadoPorPropriedadeInexistente(ex)) {
            return build(HttpStatus.BAD_REQUEST,
                    "Parâmetro de ordenação (sort) inválido: propriedade inexistente na entidade", request);
        }
        log.error("Erro inesperado (InvalidDataAccessApiUsageException)", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado", request);
    }

    private boolean causadoPorPropriedadeInexistente(Throwable ex) {
        Throwable atual = ex;
        while (atual != null) {
            if (atual instanceof org.hibernate.query.SemanticException
                    || atual instanceof org.hibernate.query.sqm.PathElementException) {
                return true;
            }
            atual = atual.getCause();
        }
        return false;
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
