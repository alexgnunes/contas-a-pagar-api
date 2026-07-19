package br.com.alexnunes.contaspagar.domain.importacao;

import java.util.List;

public class GeradorCsvErros {

    private static final String DELIMITADOR = ";";
    private static final String CABECALHO =
            "descricao;valor;dataVencimento;dataPagamento;situacao;fornecedorId";

    public String gerar(List<ImportacaoErro> erros) {
        StringBuilder csv = new StringBuilder();
        csv.append(CABECALHO).append(DELIMITADOR).append("erro").append('\n');

        for (ImportacaoErro erro : erros) {
            csv.append(erro.getLinhaOriginal())
                    .append(DELIMITADOR)
                    .append(escaparMensagem(erro.getMensagem()))
                    .append('\n');
        }

        return csv.toString();
    }

    private String escaparMensagem(String mensagem) {
        return mensagem.replace(DELIMITADOR, ",");
    }

}
