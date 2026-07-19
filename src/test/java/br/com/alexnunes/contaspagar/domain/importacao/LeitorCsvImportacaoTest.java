package br.com.alexnunes.contaspagar.domain.importacao;

import br.com.alexnunes.contaspagar.domain.importacao.exception.CsvInvalidoException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LeitorCsvImportacaoTest {

    private final LeitorCsvImportacao leitor = new LeitorCsvImportacao();

    private InputStream csv(String conteudo) {
        return new ByteArrayInputStream(conteudo.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void deveLerLinhasValidasComColunasNaOrdemDoContrato() {
        String conteudo = """
                descricao;valor;dataVencimento;dataPagamento;situacao;fornecedorId
                Energia;350.00;2026-08-10;;PENDENTE;1
                Internet;120.50;2026-08-15;2026-08-14;PAGO;2
                """;

        List<LinhaCsvImportacao> linhas = leitor.ler(csv(conteudo));

        assertThat(linhas).hasSize(2);
        assertThat(linhas.get(0).numeroLinha()).isEqualTo(2);
        assertThat(linhas.get(0).descricao()).isEqualTo("Energia");
        assertThat(linhas.get(0).valor()).isEqualTo("350.00");
        assertThat(linhas.get(0).dataPagamento()).isEmpty();
        assertThat(linhas.get(0).linhaOriginal()).isEqualTo("Energia;350.00;2026-08-10;;PENDENTE;1");
        assertThat(linhas.get(1).numeroLinha()).isEqualTo(3);
        assertThat(linhas.get(1).situacao()).isEqualTo("PAGO");
        assertThat(linhas.get(1).fornecedorId()).isEqualTo("2");
    }

    @Test
    void deveReprovarArquivoComCabecalhoForaDeOrdem() {
        String conteudo = """
                fornecedorId;situacao;descricao;dataPagamento;dataVencimento;valor
                1;PENDENTE;Energia;;2026-08-10;350.00
                """;

        assertThatThrownBy(() -> leitor.ler(csv(conteudo)))
                .isInstanceOf(CsvInvalidoException.class)
                .hasMessageContaining("Cabeçalho do CSV não corresponde à ordem esperada");
    }

    @Test
    void deveIgnorarColunaExtraNaoReconhecida() {
        String conteudo = """
                descricao;valor;dataVencimento;dataPagamento;situacao;fornecedorId;erro
                Energia;350;2026-08-10;;PENDENTE;99;Fornecedor inexistente
                """;

        List<LinhaCsvImportacao> linhas = leitor.ler(csv(conteudo));

        assertThat(linhas).hasSize(1);
        assertThat(linhas.get(0).descricao()).isEqualTo("Energia");
        assertThat(linhas.get(0).fornecedorId()).isEqualTo("99");
    }

    @Test
    void deveIgnorarLinhasEmBranco() {
        String conteudo = """
                descricao;valor;dataVencimento;dataPagamento;situacao;fornecedorId
                Energia;350.00;2026-08-10;;PENDENTE;1

                Internet;120.50;2026-08-15;2026-08-14;PAGO;2
                """;

        List<LinhaCsvImportacao> linhas = leitor.ler(csv(conteudo));

        assertThat(linhas).hasSize(2);
    }

    @Test
    void naoDeveLerArquivoSemCabecalho() {
        assertThatThrownBy(() -> leitor.ler(csv("")))
                .isInstanceOf(CsvInvalidoException.class);
    }

    @Test
    void naoDeveLerQuandoColunaObrigatoriaEstaAusente() {
        String conteudo = """
                descricao;valor;dataVencimento;situacao;fornecedorId
                Energia;350.00;2026-08-10;PENDENTE;1
                """;

        assertThatThrownBy(() -> leitor.ler(csv(conteudo)))
                .isInstanceOf(CsvInvalidoException.class)
                .hasMessageContaining("Cabeçalho do CSV não corresponde à ordem esperada");
    }

}
