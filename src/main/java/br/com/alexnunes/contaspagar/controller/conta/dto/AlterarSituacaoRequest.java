package br.com.alexnunes.contaspagar.controller.conta.dto;

import br.com.alexnunes.contaspagar.domain.conta.enums.Situacao;
import jakarta.validation.constraints.NotNull;

public record AlterarSituacaoRequest(@NotNull Situacao situacao) {
}
