package br.com.alexnunes.contaspagar.controller.conta.dto;

import br.com.alexnunes.contaspagar.domain.conta.enums.Situacao;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AlterarSituacaoRequest(@NotNull(message = "é obrigatória") Situacao situacao,
                                      LocalDate dataPagamento) {
}
