package br.com.alexnunes.contaspagar.domain.importacao;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GeradorCsvErrosTest {

    private final GeradorCsvErros gerador = new GeradorCsvErros();
    private final Importacao importacao = new Importacao("ABC123", "/dados/arquivo.csv");

    @Test
    void deveGerarCsvApenasComLinhasComErro() {
        List<ImportacaoErro> erros = List.of(
                new ImportacaoErro(importacao, 2, "Fornecedor inexistente",
                        "Energia;350;2026-08-10;;PENDENTE;99"),
                new ImportacaoErro(importacao, 3, "Valor deve ser positivo",
                        "Internet;-50;2026-08-15;;PENDENTE;2"));

        String csvGerado = gerador.gerar(erros);

        String esperado = """
                descricao;valor;dataVencimento;dataPagamento;situacao;fornecedorId;erro
                Energia;350;2026-08-10;;PENDENTE;99;Fornecedor inexistente
                Internet;-50;2026-08-15;;PENDENTE;2;Valor deve ser positivo
                """;
        assertThat(csvGerado).isEqualTo(esperado);
    }

    @Test
    void deveEscaparDelimitadorNaMensagemDeErro() {
        List<ImportacaoErro> erros = List.of(
                new ImportacaoErro(importacao, 2, "situacao inválida: PENDENTE; PAGO ou CANCELADO",
                        "Energia;350;2026-08-10;;PENDENTE;99"));

        String csvGerado = gerador.gerar(erros);

        assertThat(csvGerado).contains("situacao inválida: PENDENTE, PAGO ou CANCELADO");
    }

    @Test
    void deveGerarApenasCabecalhoQuandoNaoHaErros() {
        String csvGerado = gerador.gerar(List.of());

        assertThat(csvGerado).isEqualTo("descricao;valor;dataVencimento;dataPagamento;situacao;fornecedorId;erro\n");
    }

}
