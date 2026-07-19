package br.com.alexnunes.contaspagar.domain.importacao.exception;

public class LinhaImportacaoInvalidaException extends RuntimeException {

    public LinhaImportacaoInvalidaException(String mensagem) {
        super(mensagem);
    }

}
