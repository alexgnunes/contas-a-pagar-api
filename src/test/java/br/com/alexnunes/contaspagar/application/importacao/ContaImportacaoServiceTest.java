package br.com.alexnunes.contaspagar.application.importacao;

import br.com.alexnunes.contaspagar.application.conta.ContaService;
import br.com.alexnunes.contaspagar.domain.conta.enums.Situacao;
import br.com.alexnunes.contaspagar.domain.fornecedor.exception.FornecedorNaoEncontradoException;
import br.com.alexnunes.contaspagar.domain.importacao.LeitorCsvImportacao;
import br.com.alexnunes.contaspagar.domain.importacao.LinhaCsvImportacao;
import br.com.alexnunes.contaspagar.domain.importacao.exception.LinhaImportacaoInvalidaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContaImportacaoServiceTest {

    @Mock
    private ContaService contaService;

    private ContaImportacaoService contaImportacaoService;
    private final UUID fornecedorId = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @BeforeEach
    void setUp() {
        contaImportacaoService = new ContaImportacaoService(contaService);
    }

    @Test
    void deveCriarContaPendenteQuandoLinhaValida() {
        LinhaCsvImportacao linha = new LinhaCsvImportacao(2, "Energia", "350.00", "2026-08-10", "",
                "PENDENTE", fornecedorId.toString());

        contaImportacaoService.processarLinha(linha);

        verify(contaService).criar("Energia", new BigDecimal("350.00"), LocalDate.of(2026, 8, 10),
                fornecedorId, Situacao.PENDENTE, null);
    }

    @Test
    void deveCriarContaPagaQuandoLinhaValida() {
        LocalDate dataPagamento = LocalDate.now().minusDays(1);
        LinhaCsvImportacao linha = new LinhaCsvImportacao(3, "Internet", "120.50", "2026-08-15",
                dataPagamento.toString(), "PAGO", fornecedorId.toString());

        contaImportacaoService.processarLinha(linha);

        verify(contaService).criar("Internet", new BigDecimal("120.50"), LocalDate.of(2026, 8, 15),
                fornecedorId, Situacao.PAGO, dataPagamento);
    }

    @Test
    void naoDeveChamarContaServiceQuandoLinhaInvalida() {
        LinhaCsvImportacao linha = new LinhaCsvImportacao(4, "Internet", "-50", "2026-08-15", "",
                "PENDENTE", fornecedorId.toString());

        assertThatThrownBy(() -> contaImportacaoService.processarLinha(linha))
                .isInstanceOf(LinhaImportacaoInvalidaException.class)
                .hasMessageContaining("positivo");

        verify(contaService, never()).criar(any(), any(), any(), any(), any(), any());
    }

    @Test
    void deveCriarContaCanceladaQuandoLinhaValida() {
        LinhaCsvImportacao linha = new LinhaCsvImportacao(5, "Internet", "120.50", "2026-08-15", "",
                "CANCELADO", fornecedorId.toString());

        contaImportacaoService.processarLinha(linha);

        verify(contaService).criar("Internet", new BigDecimal("120.50"), LocalDate.of(2026, 8, 15),
                fornecedorId, Situacao.CANCELADO, null);
    }

    @Test
    void deveProcessarLinhaVindaDeReimportacaoDoCsvDeErrosComColunaExtra() {
        String csvReimportado = """
                descricao;valor;dataVencimento;dataPagamento;situacao;fornecedorId;erro
                Energia;350.00;2026-08-10;;PENDENTE;11111111-1111-1111-1111-111111111111;Fornecedor inexistente
                """;
        LeitorCsvImportacao leitor = new LeitorCsvImportacao();
        List<LinhaCsvImportacao> linhas = leitor.ler(
                new ByteArrayInputStream(csvReimportado.getBytes(StandardCharsets.UTF_8)));

        contaImportacaoService.processarLinha(linhas.get(0));

        verify(contaService).criar("Energia", new BigDecimal("350.00"), LocalDate.of(2026, 8, 10),
                fornecedorId, Situacao.PENDENTE, null);
    }

    @Test
    void devePropagarFornecedorNaoEncontradoDoContaService() {
        LinhaCsvImportacao linha = new LinhaCsvImportacao(6, "Internet", "120.50", "2026-08-15", "",
                "PENDENTE", fornecedorId.toString());
        when(contaService.criar(eq("Internet"), eq(new BigDecimal("120.50")), eq(LocalDate.of(2026, 8, 15)),
                eq(fornecedorId), eq(Situacao.PENDENTE), eq(null)))
                .thenThrow(new FornecedorNaoEncontradoException(fornecedorId));

        assertThatThrownBy(() -> contaImportacaoService.processarLinha(linha))
                .isInstanceOf(FornecedorNaoEncontradoException.class);
    }

}
