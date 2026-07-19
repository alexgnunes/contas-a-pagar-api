package br.com.alexnunes.contaspagar.domain.importacao.exception;

public class ImportacaoNaoEncontradaException extends RuntimeException {

    public ImportacaoNaoEncontradaException(String protocolo) {
        super(String.format("Importação não encontrada para o protocolo: %s", protocolo));
    }

}
