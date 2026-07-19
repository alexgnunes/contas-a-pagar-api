package br.com.alexnunes.contaspagar.domain.conta;

import br.com.alexnunes.contaspagar.domain.conta.enums.Situacao;
import br.com.alexnunes.contaspagar.domain.conta.exception.DataPagamentoInvalidaException;
import br.com.alexnunes.contaspagar.domain.conta.exception.SituacaoInvalidaException;
import br.com.alexnunes.contaspagar.domain.conta.exception.ValorInvalidoException;
import br.com.alexnunes.contaspagar.domain.fornecedor.Fornecedor;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContaTest {

    private final Fornecedor fornecedor = new Fornecedor("Fornecedor Teste");

    private Conta novaConta() {
        return Conta.criarPendente("Energia", new BigDecimal("350.00"), LocalDate.of(2026, 8, 10), fornecedor);
    }

    @Test
    void deveNascerPendenteSemDataPagamento() {
        Conta conta = novaConta();

        assertThat(conta.getSituacao()).isEqualTo(Situacao.PENDENTE);
        assertThat(conta.getDataPagamento()).isNull();
    }

    @Test
    void naoDeveCriarContaComValorZero() {
        assertThatThrownBy(() -> Conta.criarPendente("Energia", BigDecimal.ZERO, LocalDate.now(), fornecedor))
                .isInstanceOf(ValorInvalidoException.class);
    }

    @Test
    void naoDeveCriarContaComValorNegativo() {
        assertThatThrownBy(() -> Conta.criarPendente("Energia", new BigDecimal("-10.00"), LocalDate.now(), fornecedor))
                .isInstanceOf(ValorInvalidoException.class);
    }

    @Test
    void naoDeveCriarContaComValorNulo() {
        assertThatThrownBy(() -> Conta.criarPendente("Energia", null, LocalDate.now(), fornecedor))
                .isInstanceOf(ValorInvalidoException.class);
    }

    @Test
    void deveNascerPagaComDataPagamentoInformada() {
        LocalDate dataPagamento = LocalDate.now().minusDays(2);

        Conta conta = Conta.criarPaga("Energia", new BigDecimal("350.00"), LocalDate.of(2026, 8, 10),
                dataPagamento, fornecedor);

        assertThat(conta.getSituacao()).isEqualTo(Situacao.PAGO);
        assertThat(conta.getDataPagamento()).isEqualTo(dataPagamento);
    }

    @Test
    void naoDeveNascerPagaSemDataPagamento() {
        assertThatThrownBy(() -> Conta.criarPaga("Energia", new BigDecimal("350.00"),
                LocalDate.of(2026, 8, 10), null, fornecedor))
                .isInstanceOf(DataPagamentoInvalidaException.class);
    }

    @Test
    void naoDeveNascerPagaComDataPagamentoFutura() {
        LocalDate dataFutura = LocalDate.now().plusDays(1);

        assertThatThrownBy(() -> Conta.criarPaga("Energia", new BigDecimal("350.00"),
                LocalDate.of(2026, 8, 10), dataFutura, fornecedor))
                .isInstanceOf(DataPagamentoInvalidaException.class);
    }

    @Test
    void deveNascerCanceladaSemDataPagamento() {
        Conta conta = Conta.criarCancelada("Assinatura antiga", new BigDecimal("89.90"),
                LocalDate.of(2026, 5, 10), fornecedor);

        assertThat(conta.getSituacao()).isEqualTo(Situacao.CANCELADO);
        assertThat(conta.getDataPagamento()).isNull();
    }

    @Test
    void devePagarContaPendente() {
        Conta conta = novaConta();

        conta.pagar();

        assertThat(conta.getSituacao()).isEqualTo(Situacao.PAGO);
        assertThat(conta.getDataPagamento()).isEqualTo(LocalDate.now());
    }

    @Test
    void naoDevePagarContaCancelada() {
        Conta conta = novaConta();
        conta.cancelar();

        assertThatThrownBy(conta::pagar)
                .isInstanceOf(SituacaoInvalidaException.class)
                .hasMessage("Conta cancelada não pode ser paga");
    }

    @Test
    void naoDevePagarContaJaPaga() {
        Conta conta = novaConta();
        conta.pagar();

        assertThatThrownBy(conta::pagar)
                .isInstanceOf(SituacaoInvalidaException.class)
                .hasMessage("Conta já está paga");
    }

    @Test
    void deveCancelarContaPendente() {
        Conta conta = novaConta();

        conta.cancelar();

        assertThat(conta.getSituacao()).isEqualTo(Situacao.CANCELADO);
    }

    @Test
    void naoDeveCancelarContaPaga() {
        Conta conta = novaConta();
        conta.pagar();

        assertThatThrownBy(conta::cancelar)
                .isInstanceOf(SituacaoInvalidaException.class)
                .hasMessage("Conta paga não pode ser cancelada");
    }

    @Test
    void naoDeveCancelarContaJaCancelada() {
        Conta conta = novaConta();
        conta.cancelar();

        assertThatThrownBy(conta::cancelar)
                .isInstanceOf(SituacaoInvalidaException.class)
                .hasMessage("Conta já está cancelada");
    }

    @Test
    void deveAlterarValorQuandoPendente() {
        Conta conta = novaConta();

        conta.alterarValor(new BigDecimal("500.00"));

        assertThat(conta.getValor()).isEqualByComparingTo("500.00");
    }

    @Test
    void naoDeveAlterarValorQuandoNaoPendente() {
        Conta conta = novaConta();
        conta.pagar();

        assertThatThrownBy(() -> conta.alterarValor(new BigDecimal("500.00")))
                .isInstanceOf(SituacaoInvalidaException.class)
                .hasMessage("Valor só pode ser alterado enquanto a conta estiver pendente");
    }

    @Test
    void naoDeveAlterarValorParaNegativo() {
        Conta conta = novaConta();

        assertThatThrownBy(() -> conta.alterarValor(new BigDecimal("-1.00")))
                .isInstanceOf(ValorInvalidoException.class);
    }

    @Test
    void deveTrocarFornecedorQuandoPendente() {
        Conta conta = novaConta();
        Fornecedor novoFornecedor = new Fornecedor("Outro Fornecedor");

        conta.trocarFornecedor(novoFornecedor);

        assertThat(conta.getFornecedor()).isEqualTo(novoFornecedor);
    }

    @Test
    void naoDeveTrocarFornecedorQuandoNaoPendente() {
        Conta conta = novaConta();
        conta.cancelar();
        Fornecedor novoFornecedor = new Fornecedor("Outro Fornecedor");

        assertThatThrownBy(() -> conta.trocarFornecedor(novoFornecedor))
                .isInstanceOf(SituacaoInvalidaException.class)
                .hasMessage("Fornecedor só pode ser alterado enquanto a conta estiver pendente");
    }

}
