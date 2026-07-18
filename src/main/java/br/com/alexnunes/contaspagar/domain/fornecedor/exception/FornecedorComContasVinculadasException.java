package br.com.alexnunes.contaspagar.domain.fornecedor.exception;

import java.util.UUID;

public class FornecedorComContasVinculadasException extends RuntimeException {

    public FornecedorComContasVinculadasException(UUID id) {
        super("Fornecedor possui contas vinculadas e não pode ser excluído: " + id);
    }

}
