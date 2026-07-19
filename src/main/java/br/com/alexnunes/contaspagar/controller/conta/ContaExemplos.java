package br.com.alexnunes.contaspagar.controller.conta;

final class ContaExemplos {

    static final String CONTA_VALIDA = """
            {
              "descricao": "Energia",
              "valor": 350.00,
              "dataVencimento": "2026-08-10",
              "fornecedorId": "11111111-1111-1111-1111-111111111111"
            }""";

    static final String VALOR_NEGATIVO = """
            {
              "descricao": "Energia",
              "valor": -350.00,
              "dataVencimento": "2026-08-10",
              "fornecedorId": "11111111-1111-1111-1111-111111111111"
            }""";

    static final String FORNECEDOR_INEXISTENTE = """
            {
              "descricao": "Energia",
              "valor": 350.00,
              "dataVencimento": "2026-08-10",
              "fornecedorId": "99999999-9999-9999-9999-999999999999"
            }""";

    private ContaExemplos() {
    }

}
