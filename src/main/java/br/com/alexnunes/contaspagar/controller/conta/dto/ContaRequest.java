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
        @NotBlank(message = "não pode estar em branco") @Size(max = 255, message = "deve ter no máximo 255 caracteres") String descricao,
        @NotNull(message = "é obrigatório") @Positive(message = "deve ser positivo") BigDecimal valor,
        @NotNull(message = "é obrigatória") LocalDate dataVencimento,
        @NotNull(message = "é obrigatório") @Schema(example = "11111111-1111-1111-1111-111111111111") UUID fornecedorId,
        @Schema(description = "Opcional, default PENDENTE. Aceita PENDENTE, PAGO ou CANCELADO.")
        Situacao situacao,
        @Schema(description = "Obrigatória somente quando situacao=PAGO; deve ficar vazia/null nos demais casos.")
        LocalDate dataPagamento) {

    public ContaRequest {
        if (situacao == null) {
            situacao = Situacao.PENDENTE;
        }
    }
}
