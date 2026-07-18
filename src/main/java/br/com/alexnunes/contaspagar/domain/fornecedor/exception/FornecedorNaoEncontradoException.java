package br.com.alexnunes.contaspagar.domain.fornecedor.exception;

import java.util.UUID;

public class FornecedorNaoEncontradoException extends RuntimeException {

    public FornecedorNaoEncontradoException(UUID id) {
        super("Fornecedor não encontrado: " + id);
    }

}
