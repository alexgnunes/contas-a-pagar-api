package br.com.alexnunes.contaspagar.domain.importacao;

public record LinhaCsvImportacao(
        int numeroLinha,
        String descricao,
        String valor,
        String dataVencimento,
        String dataPagamento,
        String situacao,
        String fornecedorId,
        String linhaOriginal) {

    public LinhaCsvImportacao(int numeroLinha, String descricao, String valor, String dataVencimento,
                               String dataPagamento, String situacao, String fornecedorId) {
        this(numeroLinha, descricao, valor, dataVencimento, dataPagamento, situacao, fornecedorId, "");
    }
}
