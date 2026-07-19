package br.com.alexnunes.contaspagar.controller.importacao.dto;

import br.com.alexnunes.contaspagar.domain.importacao.enums.ImportacaoStatus;

public record ImportacaoStatusResponse(
        String protocolo,
        ImportacaoStatus status,
        int totalRegistros,
        int sucesso,
        int falhas,
        String downloadErros) {
}
