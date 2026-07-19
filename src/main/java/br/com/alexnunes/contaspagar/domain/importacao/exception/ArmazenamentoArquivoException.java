package br.com.alexnunes.contaspagar.domain.importacao.exception;

public class ArmazenamentoArquivoException extends RuntimeException {

    public ArmazenamentoArquivoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }

}
