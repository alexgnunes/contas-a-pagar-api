package br.com.alexnunes.contaspagar.domain.conta.exception;

public class DataPagamentoInvalidaException extends RuntimeException {

    public DataPagamentoInvalidaException(String mensagem) {
        super(mensagem);
    }

}
