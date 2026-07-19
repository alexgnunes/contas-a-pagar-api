package br.com.alexnunes.contaspagar.domain.importacao;

import br.com.alexnunes.contaspagar.domain.conta.enums.Situacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record LinhaImportacaoValida(
        int numeroLinha,
        String descricao,
        BigDecimal valor,
        LocalDate dataVencimento,
        LocalDate dataPagamento,
        Situacao situacao,
        UUID fornecedorId) implements ResultadoValidacaoLinha {
}
