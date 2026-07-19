package br.com.alexnunes.contaspagar.controller.conta.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ContaRequest(
        @NotBlank @Size(max = 255) String descricao,
        @NotNull @Positive BigDecimal valor,
        @NotNull LocalDate dataVencimento,
        @NotNull UUID fornecedorId) {
}
