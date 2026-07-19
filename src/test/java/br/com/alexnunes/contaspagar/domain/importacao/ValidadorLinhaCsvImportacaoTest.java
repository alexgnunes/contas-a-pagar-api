package br.com.alexnunes.contaspagar.domain.importacao;

import br.com.alexnunes.contaspagar.domain.conta.enums.Situacao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ValidadorLinhaCsvImportacaoTest {

    private final ValidadorLinhaCsvImportacao validador = new ValidadorLinhaCsvImportacao();
    private final UUID fornecedorId = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private LinhaCsvImportacao linha(String descricao, String valor, String dataVencimento,
                                      String dataPagamento, String situacao, String fornecedorId) {
        return new LinhaCsvImportacao(2, descricao, valor, dataVencimento, dataPagamento, situacao, fornecedorId);
    }

    @Test
    void deveValidarLinhaPendenteSemDataPagamento() {
        ResultadoValidacaoLinha resultado = validador.validar(
                linha("Energia", "350.00", "2026-08-10", "", "PENDENTE", fornecedorId.toString()));

        assertThat(resultado).isInstanceOf(LinhaImportacaoValida.class);
        LinhaImportacaoValida valida = (LinhaImportacaoValida) resultado;
        assertThat(valida.numeroLinha()).isEqualTo(2);
        assertThat(valida.descricao()).isEqualTo("Energia");
        assertThat(valida.valor()).isEqualByComparingTo("350.00");
        assertThat(valida.dataVencimento()).isEqualTo(LocalDate.of(2026, 8, 10));
        assertThat(valida.dataPagamento()).isNull();
        assertThat(valida.situacao()).isEqualTo(Situacao.PENDENTE);
        assertThat(valida.fornecedorId()).isEqualTo(fornecedorId);
    }

    @Test
    void deveValidarLinhaPagaComDataPagamentoPassada() {
        LocalDate dataPagamento = LocalDate.now().minusDays(1);

        ResultadoValidacaoLinha resultado = validador.validar(
                linha("Internet", "120.50", "2026-08-15", dataPagamento.toString(), "PAGO", fornecedorId.toString()));

        assertThat(resultado).isInstanceOf(LinhaImportacaoValida.class);
        LinhaImportacaoValida valida = (LinhaImportacaoValida) resultado;
        assertThat(valida.situacao()).isEqualTo(Situacao.PAGO);
        assertThat(valida.dataPagamento()).isEqualTo(dataPagamento);
    }

    @Test
    void naoDeveValidarQuandoPagoSemDataPagamento() {
        ResultadoValidacaoLinha resultado = validador.validar(
                linha("Internet", "120.50", "2026-08-15", "", "PAGO", fornecedorId.toString()));

        assertThat(resultado).isInstanceOf(LinhaImportacaoInvalida.class);
        assertThat(((LinhaImportacaoInvalida) resultado).mensagem()).contains("obrigatória");
    }

    @Test
    void naoDeveValidarQuandoPendenteComDataPagamentoPreenchida() {
        ResultadoValidacaoLinha resultado = validador.validar(
                linha("Internet", "120.50", "2026-08-15", "2026-08-14", "PENDENTE", fornecedorId.toString()));

        assertThat(resultado).isInstanceOf(LinhaImportacaoInvalida.class);
        assertThat(((LinhaImportacaoInvalida) resultado).mensagem()).contains("deve estar vazia");
    }

    @Test
    void deveValidarLinhaCanceladaSemDataPagamento() {
        ResultadoValidacaoLinha resultado = validador.validar(
                linha("Internet", "120.50", "2026-08-15", "", "CANCELADO", fornecedorId.toString()));

        assertThat(resultado).isInstanceOf(LinhaImportacaoValida.class);
        LinhaImportacaoValida valida = (LinhaImportacaoValida) resultado;
        assertThat(valida.situacao()).isEqualTo(Situacao.CANCELADO);
        assertThat(valida.dataPagamento()).isNull();
    }

    @Test
    void naoDeveValidarQuandoCanceladoComDataPagamentoPreenchida() {
        ResultadoValidacaoLinha resultado = validador.validar(
                linha("Internet", "120.50", "2026-08-15", "2026-08-14", "CANCELADO", fornecedorId.toString()));

        assertThat(resultado).isInstanceOf(LinhaImportacaoInvalida.class);
        assertThat(((LinhaImportacaoInvalida) resultado).mensagem()).contains("deve estar vazia");
    }

    @Test
    void naoDeveValidarQuandoSituacaoForDesconhecida() {
        ResultadoValidacaoLinha resultado = validador.validar(
                linha("Internet", "120.50", "2026-08-15", "", "QUITADO", fornecedorId.toString()));

        assertThat(resultado).isInstanceOf(LinhaImportacaoInvalida.class);
        assertThat(((LinhaImportacaoInvalida) resultado).mensagem()).contains("situacao inválida");
    }

    @Test
    void naoDeveValidarValorNegativo() {
        ResultadoValidacaoLinha resultado = validador.validar(
                linha("Internet", "-50", "2026-08-15", "", "PENDENTE", fornecedorId.toString()));

        assertThat(resultado).isInstanceOf(LinhaImportacaoInvalida.class);
        assertThat(((LinhaImportacaoInvalida) resultado).mensagem()).contains("positivo");
    }

    @Test
    void naoDeveValidarValorNaoNumerico() {
        ResultadoValidacaoLinha resultado = validador.validar(
                linha("Internet", "abc", "2026-08-15", "", "PENDENTE", fornecedorId.toString()));

        assertThat(resultado).isInstanceOf(LinhaImportacaoInvalida.class);
        assertThat(((LinhaImportacaoInvalida) resultado).mensagem()).contains("Valor inválido");
    }

    @Test
    void naoDeveValidarDataVencimentoComFormatoInvalido() {
        ResultadoValidacaoLinha resultado = validador.validar(
                linha("Internet", "120.50", "15/08/2026", "", "PENDENTE", fornecedorId.toString()));

        assertThat(resultado).isInstanceOf(LinhaImportacaoInvalida.class);
        assertThat(((LinhaImportacaoInvalida) resultado).mensagem()).contains("dataVencimento inválida");
    }

    @Test
    void naoDeveValidarDataPagamentoFutura() {
        String dataFutura = LocalDate.now().plusDays(30).toString();

        ResultadoValidacaoLinha resultado = validador.validar(
                linha("Internet", "120.50", "2026-08-15", dataFutura, "PAGO", fornecedorId.toString()));

        assertThat(resultado).isInstanceOf(LinhaImportacaoInvalida.class);
        assertThat(((LinhaImportacaoInvalida) resultado).mensagem()).contains("não pode ser futura");
    }

    @Test
    void naoDeveValidarFornecedorIdComFormatoInvalido() {
        ResultadoValidacaoLinha resultado = validador.validar(
                linha("Internet", "120.50", "2026-08-15", "", "PENDENTE", "not-a-uuid"));

        assertThat(resultado).isInstanceOf(LinhaImportacaoInvalida.class);
        assertThat(((LinhaImportacaoInvalida) resultado).mensagem()).contains("fornecedorId inválido");
    }

    @Test
    void naoDeveValidarDescricaoEmBranco() {
        ResultadoValidacaoLinha resultado = validador.validar(
                linha("   ", "120.50", "2026-08-15", "", "PENDENTE", fornecedorId.toString()));

        assertThat(resultado).isInstanceOf(LinhaImportacaoInvalida.class);
        assertThat(((LinhaImportacaoInvalida) resultado).mensagem()).contains("descricao");
    }

}
