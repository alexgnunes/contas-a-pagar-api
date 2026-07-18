package br.com.alexnunes.contaspagar.domain.conta.exception;

public class SituacaoInvalidaException extends RuntimeException {

    public SituacaoInvalidaException(String mensagem) {
        super(mensagem);
    }

}
