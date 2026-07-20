package br.com.alexnunes.contaspagar.controller.conta;

final class ContaExemplos {

    static final String CONTA_VALIDA = """
            {
              "descricao": "Energia",
              "valor": 350.00,
              "dataVencimento": "2026-08-10",
              "fornecedorId": "11111111-1111-1111-1111-111111111111",
              "situacao": "PENDENTE",
              "dataPagamento": null
            }""";

    static final String CONTA_JA_PAGA = """
            {
              "descricao": "Energia",
              "valor": 350.00,
              "dataVencimento": "2026-08-10",
              "fornecedorId": "11111111-1111-1111-1111-111111111111",
              "situacao": "PAGO",
              "dataPagamento": "2026-07-10"
            }""";

    static final String VALOR_NEGATIVO = """
            {
              "descricao": "Energia",
              "valor": -350.00,
              "dataVencimento": "2026-08-10",
              "fornecedorId": "11111111-1111-1111-1111-111111111111",
              "situacao": "PENDENTE",
              "dataPagamento": null
            }""";

    static final String FORNECEDOR_INEXISTENTE = """
            {
              "descricao": "Energia",
              "valor": 350.00,
              "dataVencimento": "2026-08-10",
              "fornecedorId": "99999999-9999-9999-9999-999999999999",
              "situacao": "PENDENTE",
              "dataPagamento": null
            }""";

    private ContaExemplos() {
    }

}
