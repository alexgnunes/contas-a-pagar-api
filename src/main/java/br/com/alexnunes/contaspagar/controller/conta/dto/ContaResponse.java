package br.com.alexnunes.contaspagar.controller.conta.dto;

import br.com.alexnunes.contaspagar.controller.fornecedor.dto.FornecedorResponse;
import br.com.alexnunes.contaspagar.domain.conta.enums.Situacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ContaResponse(
        UUID id,
        String descricao,
        BigDecimal valor,
        LocalDate dataVencimento,
        LocalDate dataPagamento,
        Situacao situacao,
        FornecedorResponse fornecedor) {
}
