package br.com.alexnunes.contaspagar.domain.conta.exception;

import java.util.UUID;

public class ContaNaoEncontradaException extends RuntimeException {

    public ContaNaoEncontradaException(UUID id) {
        super(String.format("Conta não encontrada: %s", id));
    }

}
