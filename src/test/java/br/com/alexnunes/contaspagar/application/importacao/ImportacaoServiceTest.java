package br.com.alexnunes.contaspagar.application.importacao;

import br.com.alexnunes.contaspagar.domain.fornecedor.exception.FornecedorNaoEncontradoException;
import br.com.alexnunes.contaspagar.domain.importacao.ArmazenamentoArquivoImportacao;
import br.com.alexnunes.contaspagar.domain.importacao.Importacao;
import br.com.alexnunes.contaspagar.domain.importacao.ImportacaoErro;
import br.com.alexnunes.contaspagar.domain.importacao.ImportacaoErroRepository;
import br.com.alexnunes.contaspagar.domain.importacao.ImportacaoRepository;
import br.com.alexnunes.contaspagar.domain.importacao.LinhaCsvImportacao;
import br.com.alexnunes.contaspagar.domain.importacao.PublicadorImportacao;
import br.com.alexnunes.contaspagar.domain.importacao.enums.ImportacaoStatus;
import br.com.alexnunes.contaspagar.domain.importacao.exception.ImportacaoNaoEncontradaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportacaoServiceTest {

    @Mock
    private ArmazenamentoArquivoImportacao armazenamentoArquivo;

    @Mock
    private ImportacaoRepository importacaoRepository;

    @Mock
    private ImportacaoErroRepository importacaoErroRepository;

    @Mock
    private PublicadorImportacao publicadorImportacao;

    @Mock
    private ContaImportacaoService contaImportacaoService;

    private ImportacaoService importacaoService;

    @BeforeEach
    void setUp() {
        importacaoService = new ImportacaoService(armazenamentoArquivo, importacaoRepository,
                importacaoErroRepository, publicadorImportacao, contaImportacaoService);
    }

    private InputStream csv(String conteudo) {
        return new ByteArrayInputStream(conteudo.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void deveSalvarArquivoGerarProtocoloEPublicarAoIniciar() {
        when(armazenamentoArquivo.salvar(any(), eq("arquivo.csv"))).thenReturn("/dados/arquivo.csv");
        when(importacaoRepository.salvar(any(Importacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String protocolo = importacaoService.iniciar(csv("qualquer conteudo"), "arquivo.csv");

        assertThat(protocolo).isNotBlank();
        verify(publicadorImportacao).publicar(protocolo);

        ArgumentCaptor<Importacao> captor = ArgumentCaptor.forClass(Importacao.class);
        verify(importacaoRepository).salvar(captor.capture());
        assertThat(captor.getValue().getProtocolo()).isEqualTo(protocolo);
        assertThat(captor.getValue().getCaminhoArquivo()).isEqualTo("/dados/arquivo.csv");
        assertThat(captor.getValue().getStatus()).isEqualTo(ImportacaoStatus.PROCESSANDO);
    }

    @Test
    void deveConcluirComSucessoQuandoTodasAsLinhasSaoValidas() {
        Importacao importacao = new Importacao("ABC123", "/dados/arquivo.csv");
        when(importacaoRepository.buscarPorProtocolo("ABC123")).thenReturn(Optional.of(importacao));
        String conteudo = """
                descricao;valor;dataVencimento;dataPagamento;situacao;fornecedorId
                Energia;350.00;2026-08-10;;PENDENTE;11111111-1111-1111-1111-111111111111
                Internet;120.50;2026-08-15;;PENDENTE;11111111-1111-1111-1111-111111111111
                """;
        when(armazenamentoArquivo.abrir("/dados/arquivo.csv")).thenReturn(csv(conteudo));

        importacaoService.processarMensagem("ABC123");

        verify(contaImportacaoService, times(2)).processarLinha(any());
        assertThat(importacao.getStatus()).isEqualTo(ImportacaoStatus.CONCLUIDO);
        assertThat(importacao.getTotalRegistros()).isEqualTo(2);
        assertThat(importacao.getSucesso()).isEqualTo(2);
        assertThat(importacao.getFalhas()).isEqualTo(0);
        verify(importacaoRepository).salvar(importacao);
        verify(importacaoErroRepository, never()).salvar(any());
    }

    @Test
    void deveConcluirComErrosQuandoAlgumaLinhaFalha() {
        Importacao importacao = new Importacao("ABC123", "/dados/arquivo.csv");
        when(importacaoRepository.buscarPorProtocolo("ABC123")).thenReturn(Optional.of(importacao));
        String conteudo = """
                descricao;valor;dataVencimento;dataPagamento;situacao;fornecedorId
                Energia;350.00;2026-08-10;;PENDENTE;11111111-1111-1111-1111-111111111111
                Internet;120.50;2026-08-15;;PENDENTE;99999999-9999-9999-9999-999999999999
                """;
        when(armazenamentoArquivo.abrir("/dados/arquivo.csv")).thenReturn(csv(conteudo));
        doAnswer(invocation -> {
            LinhaCsvImportacao linha = invocation.getArgument(0);
            if (linha.numeroLinha() == 3) {
                throw new FornecedorNaoEncontradoException(UUID.fromString("99999999-9999-9999-9999-999999999999"));
            }
            return null;
        }).when(contaImportacaoService).processarLinha(any());

        importacaoService.processarMensagem("ABC123");

        assertThat(importacao.getStatus()).isEqualTo(ImportacaoStatus.CONCLUIDO_COM_ERROS);
        assertThat(importacao.getTotalRegistros()).isEqualTo(2);
        assertThat(importacao.getSucesso()).isEqualTo(1);
        assertThat(importacao.getFalhas()).isEqualTo(1);

        ArgumentCaptor<ImportacaoErro> captor = ArgumentCaptor.forClass(ImportacaoErro.class);
        verify(importacaoErroRepository).salvar(captor.capture());
        assertThat(captor.getValue().getLinha()).isEqualTo(3);
        assertThat(captor.getValue().getMensagem()).contains("Fornecedor não encontrado");
        assertThat(captor.getValue().getImportacao()).isSameAs(importacao);
        assertThat(captor.getValue().getLinhaOriginal())
                .isEqualTo("Internet;120.50;2026-08-15;;PENDENTE;99999999-9999-9999-9999-999999999999");
    }

    @Test
    void deveFalharQuandoArquivoTemErroEstrutural() {
        Importacao importacao = new Importacao("ABC123", "/dados/arquivo.csv");
        when(importacaoRepository.buscarPorProtocolo("ABC123")).thenReturn(Optional.of(importacao));
        when(armazenamentoArquivo.abrir("/dados/arquivo.csv")).thenReturn(csv(""));

        importacaoService.processarMensagem("ABC123");

        assertThat(importacao.getStatus()).isEqualTo(ImportacaoStatus.FALHOU);
        verify(contaImportacaoService, never()).processarLinha(any());
        verify(importacaoRepository).salvar(importacao);
    }

    @Test
    void deveDescartarMensagemSemLancarQuandoProtocoloNaoEncontrado() {
        when(importacaoRepository.buscarPorProtocolo("XYZ")).thenReturn(Optional.empty());

        importacaoService.processarMensagem("XYZ");

        verify(contaImportacaoService, never()).processarLinha(any());
        verify(importacaoRepository, never()).salvar(any());
    }

    @Test
    void deveMarcarComoFalhouQuandoConcluirLancaExcecaoInesperada() {
        Importacao importacao = new Importacao("ABC123", "/dados/arquivo.csv");
        when(importacaoRepository.buscarPorProtocolo("ABC123")).thenReturn(Optional.of(importacao));
        String conteudo = """
                descricao;valor;dataVencimento;dataPagamento;situacao;fornecedorId
                Energia;350.00;2026-08-10;;PENDENTE;11111111-1111-1111-1111-111111111111
                """;
        when(armazenamentoArquivo.abrir("/dados/arquivo.csv")).thenReturn(csv(conteudo));
        when(importacaoRepository.salvar(importacao))
                .thenThrow(new RuntimeException("falha inesperada no banco"))
                .thenReturn(importacao);

        importacaoService.processarMensagem("ABC123");

        assertThat(importacao.getStatus()).isEqualTo(ImportacaoStatus.FALHOU);
    }

    @Test
    void deveConsultarStatusPeloProtocolo() {
        Importacao importacao = new Importacao("ABC123", "/dados/arquivo.csv");
        importacao.concluir(2, 1, 1);
        when(importacaoRepository.buscarPorProtocolo("ABC123")).thenReturn(Optional.of(importacao));

        Importacao resultado = importacaoService.consultarStatus("ABC123");

        assertThat(resultado).isSameAs(importacao);
    }

    @Test
    void naoDeveConsultarStatusQuandoProtocoloNaoEncontrado() {
        when(importacaoRepository.buscarPorProtocolo("XYZ")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> importacaoService.consultarStatus("XYZ"))
                .isInstanceOf(ImportacaoNaoEncontradaException.class);
    }

    @Test
    void deveGerarCsvDeErrosApenasComErrosPersistidos() {
        Importacao importacao = new Importacao("ABC123", "/dados/arquivo.csv");
        when(importacaoRepository.buscarPorProtocolo("ABC123")).thenReturn(Optional.of(importacao));
        when(importacaoErroRepository.buscarPorImportacaoId(importacao.getId())).thenReturn(List.of(
                new ImportacaoErro(importacao, 3, "Valor deve ser positivo",
                        "Internet;-50;2026-08-15;;PENDENTE;2")));

        String csvErros = importacaoService.gerarCsvErros("ABC123");

        assertThat(csvErros).contains("descricao;valor;dataVencimento;dataPagamento;situacao;fornecedorId;erro");
        assertThat(csvErros).contains("Internet;-50;2026-08-15;;PENDENTE;2;Valor deve ser positivo");
        assertThat(csvErros).doesNotContain("Energia");
        verify(armazenamentoArquivo, never()).abrir("/dados/arquivo.csv");
    }

}
