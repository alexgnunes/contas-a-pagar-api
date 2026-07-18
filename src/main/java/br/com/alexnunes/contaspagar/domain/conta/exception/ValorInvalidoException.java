package br.com.alexnunes.contaspagar.domain.conta.exception;

public class ValorInvalidoException extends RuntimeException {

    public ValorInvalidoException(String mensagem) {
        super(mensagem);
    }

}
