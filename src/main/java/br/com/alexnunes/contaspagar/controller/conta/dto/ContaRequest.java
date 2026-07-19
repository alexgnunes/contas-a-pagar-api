package br.com.alexnunes.contaspagar.controller.conta.dto;

import br.com.alexnunes.contaspagar.domain.conta.enums.Situacao;
import io.swagger.v3.oas.annotations.media.Schema;
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
        @NotNull @Schema(example = "11111111-1111-1111-1111-111111111111") UUID fornecedorId,
        Situacao situacao,
        LocalDate dataPagamento) {

    public ContaRequest {
        if (situacao == null) {
            situacao = Situacao.PENDENTE;
        }
    }
}
