package br.com.alexnunes.contaspagar.domain.importacao;

import br.com.alexnunes.contaspagar.domain.conta.enums.Situacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.UUID;

public class ValidadorLinhaCsvImportacao {

    public ResultadoValidacaoLinha validar(LinhaCsvImportacao linha) {
        try {
            String descricao = validarDescricao(linha.descricao());
            BigDecimal valor = validarValor(linha.valor());
            LocalDate dataVencimento = validarData(linha.dataVencimento(), "dataVencimento");
            Situacao situacao = validarSituacao(linha.situacao());
            LocalDate dataPagamento = validarDataPagamento(linha.dataPagamento(), situacao);
            UUID fornecedorId = validarFornecedorId(linha.fornecedorId());

            return new LinhaImportacaoValida(linha.numeroLinha(), descricao, valor, dataVencimento,
                    dataPagamento, situacao, fornecedorId);
        } catch (LinhaInvalidaException e) {
            return new LinhaImportacaoInvalida(linha.numeroLinha(), e.getMessage());
        }
    }

    private String validarDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            throw new LinhaInvalidaException("descricao é obrigatória");
        }
        return descricao;
    }

    private BigDecimal validarValor(String valor) {
        BigDecimal valorConvertido;
        try {
            valorConvertido = new BigDecimal(valor.trim());
        } catch (NumberFormatException | NullPointerException e) {
            throw new LinhaInvalidaException(String.format("Valor inválido: %s", valor));
        }

        if (valorConvertido.signum() <= 0) {
            throw new LinhaInvalidaException("Valor deve ser positivo");
        }

        return valorConvertido;
    }

    private LocalDate validarData(String data, String nomeCampo) {
        try {
            return LocalDate.parse(data.trim());
        } catch (DateTimeParseException | NullPointerException e) {
            throw new LinhaInvalidaException(String.format("%s inválida: %s", nomeCampo, data));
        }
    }

    private Situacao validarSituacao(String situacao) {
        String valorNormalizado = situacao == null ? "" : situacao.trim().toUpperCase();

        boolean valida = valorNormalizado.equals(Situacao.PENDENTE.name())
                || valorNormalizado.equals(Situacao.PAGO.name())
                || valorNormalizado.equals(Situacao.CANCELADO.name());

        if (!valida) {
            throw new LinhaInvalidaException(String.format("situacao inválida: %s", situacao));
        }

        return Situacao.valueOf(valorNormalizado);
    }

    private LocalDate validarDataPagamento(String dataPagamento, Situacao situacao) {
        boolean informada = dataPagamento != null && !dataPagamento.isBlank();

        if (situacao == Situacao.PENDENTE || situacao == Situacao.CANCELADO) {
            if (informada) {
                throw new LinhaInvalidaException(
                        String.format("dataPagamento deve estar vazia quando situacao=%s", situacao));
            }
            return null;
        }

        if (!informada) {
            throw new LinhaInvalidaException("dataPagamento é obrigatória quando situacao=PAGO");
        }

        LocalDate dataConvertida = validarData(dataPagamento, "dataPagamento");
        if (dataConvertida.isAfter(LocalDate.now())) {
            throw new LinhaInvalidaException("dataPagamento não pode ser futura");
        }

        return dataConvertida;
    }

    private UUID validarFornecedorId(String fornecedorId) {
        try {
            return UUID.fromString(fornecedorId.trim());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new LinhaInvalidaException(String.format("fornecedorId inválido: %s", fornecedorId));
        }
    }

    private static class LinhaInvalidaException extends RuntimeException {
        LinhaInvalidaException(String mensagem) {
            super(mensagem);
        }
    }

}
