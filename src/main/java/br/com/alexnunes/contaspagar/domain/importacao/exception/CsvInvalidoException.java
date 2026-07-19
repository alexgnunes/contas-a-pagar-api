package br.com.alexnunes.contaspagar.domain.importacao.exception;

public class CsvInvalidoException extends RuntimeException {

    public CsvInvalidoException(String mensagem) {
        super(mensagem);
    }

    public CsvInvalidoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }

}
