package br.com.alexnunes.contaspagar.domain.fornecedor.exception;

import java.util.UUID;

public class FornecedorComContasVinculadasException extends RuntimeException {

    public FornecedorComContasVinculadasException(UUID id) {
        super(String.format("Fornecedor possui contas vinculadas e não pode ser excluído: %s", id));
    }

}
