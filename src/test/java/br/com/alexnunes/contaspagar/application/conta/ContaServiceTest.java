package br.com.alexnunes.contaspagar.application.conta;

import br.com.alexnunes.contaspagar.domain.conta.Conta;
import br.com.alexnunes.contaspagar.domain.conta.ContaRepository;
import br.com.alexnunes.contaspagar.domain.conta.PeriodoFiltro;
import br.com.alexnunes.contaspagar.domain.conta.enums.Situacao;
import br.com.alexnunes.contaspagar.domain.conta.exception.ContaNaoEncontradaException;
import br.com.alexnunes.contaspagar.domain.conta.exception.IntervaloDataInvalidoException;
import br.com.alexnunes.contaspagar.domain.conta.exception.SituacaoInvalidaException;
import br.com.alexnunes.contaspagar.domain.fornecedor.Fornecedor;
import br.com.alexnunes.contaspagar.domain.fornecedor.FornecedorRepository;
import br.com.alexnunes.contaspagar.domain.fornecedor.exception.FornecedorNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContaServiceTest {

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private FornecedorRepository fornecedorRepository;

    private ContaService contaService;
    private Fornecedor fornecedor;

    @BeforeEach
    void setUp() {
        contaService = new ContaService(contaRepository, fornecedorRepository);
        fornecedor = new Fornecedor("Fornecedor Teste");
        ReflectionTestUtils.setField(fornecedor, "id", UUID.randomUUID());
    }

    private void stubSalvarRetornandoOMesmo() {
        when(contaRepository.salvar(any(Conta.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Conta novaConta() {
        return Conta.criarPendente("Energia", new BigDecimal("350.00"), LocalDate.of(2026, 8, 10), fornecedor);
    }

    @Test
    void deveCriarContaComFornecedorExistente() {
        stubSalvarRetornandoOMesmo();
        when(fornecedorRepository.buscarPorId(fornecedor.getId())).thenReturn(Optional.of(fornecedor));

        Conta conta = contaService.criar("Energia", new BigDecimal("350.00"), LocalDate.of(2026, 8, 10),
                fornecedor.getId());

        assertThat(conta.getDescricao()).isEqualTo("Energia");
        assertThat(conta.getSituacao()).isEqualTo(Situacao.PENDENTE);
    }

    @Test
    void deveCriarContaPendenteViaOverloadDeImportacaoQuandoSituacaoPendente() {
        stubSalvarRetornandoOMesmo();
        when(fornecedorRepository.buscarPorId(fornecedor.getId())).thenReturn(Optional.of(fornecedor));

        Conta conta = contaService.criar("Energia", new BigDecimal("350.00"), LocalDate.of(2026, 8, 10),
                fornecedor.getId(), Situacao.PENDENTE, null);

        assertThat(conta.getSituacao()).isEqualTo(Situacao.PENDENTE);
        assertThat(conta.getDataPagamento()).isNull();
    }

    @Test
    void deveCriarContaPagaViaOverloadDeImportacaoQuandoSituacaoPaga() {
        stubSalvarRetornandoOMesmo();
        when(fornecedorRepository.buscarPorId(fornecedor.getId())).thenReturn(Optional.of(fornecedor));
        LocalDate dataPagamento = LocalDate.now().minusDays(1);

        Conta conta = contaService.criar("Energia", new BigDecimal("350.00"), LocalDate.of(2026, 8, 10),
                fornecedor.getId(), Situacao.PAGO, dataPagamento);

        assertThat(conta.getSituacao()).isEqualTo(Situacao.PAGO);
        assertThat(conta.getDataPagamento()).isEqualTo(dataPagamento);
    }

    @Test
    void deveCriarContaCanceladaViaOverloadDeImportacaoQuandoSituacaoCancelada() {
        stubSalvarRetornandoOMesmo();
        when(fornecedorRepository.buscarPorId(fornecedor.getId())).thenReturn(Optional.of(fornecedor));

        Conta conta = contaService.criar("Energia", new BigDecimal("350.00"), LocalDate.of(2026, 8, 10),
                fornecedor.getId(), Situacao.CANCELADO, null);

        assertThat(conta.getSituacao()).isEqualTo(Situacao.CANCELADO);
        assertThat(conta.getDataPagamento()).isNull();
    }

    @Test
    void naoDeveCriarContaComFornecedorInexistente() {
        UUID fornecedorId = UUID.randomUUID();
        when(fornecedorRepository.buscarPorId(fornecedorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contaService.criar("Energia", new BigDecimal("350.00"), LocalDate.now(), fornecedorId))
                .isInstanceOf(FornecedorNaoEncontradoException.class);
    }

    @Test
    void deveBuscarPorIdQuandoExiste() {
        Conta conta = novaConta();
        UUID id = UUID.randomUUID();
        when(contaRepository.buscarPorId(id)).thenReturn(Optional.of(conta));

        assertThat(contaService.buscarPorId(id)).isEqualTo(conta);
    }

    @Test
    void naoDeveBuscarPorIdQuandoNaoExiste() {
        UUID id = UUID.randomUUID();
        when(contaRepository.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contaService.buscarPorId(id)).isInstanceOf(ContaNaoEncontradaException.class);
    }

    @Test
    void deveAtualizarDescricaoEDataVencimentoQuandoPendente() {
        stubSalvarRetornandoOMesmo();
        Conta conta = novaConta();
        UUID id = UUID.randomUUID();
        when(contaRepository.buscarPorId(id)).thenReturn(Optional.of(conta));

        Conta atualizada = contaService.atualizar(id, "Nova descricao", conta.getValor(),
                LocalDate.of(2026, 9, 1), fornecedor.getId());

        assertThat(atualizada.getDescricao()).isEqualTo("Nova descricao");
        assertThat(atualizada.getDataVencimento()).isEqualTo(LocalDate.of(2026, 9, 1));
        verify(fornecedorRepository, never()).buscarPorId(any());
    }

    @Test
    void naoDeveAtualizarDescricaoQuandoNaoPendente() {
        Conta conta = novaConta();
        conta.pagar();
        UUID id = UUID.randomUUID();
        when(contaRepository.buscarPorId(id)).thenReturn(Optional.of(conta));

        assertThatThrownBy(() -> contaService.atualizar(id, "Nova descricao", conta.getValor(),
                conta.getDataVencimento(), fornecedor.getId()))
                .isInstanceOf(SituacaoInvalidaException.class);
    }

    @Test
    void deveAtualizarValorQuandoPendente() {
        stubSalvarRetornandoOMesmo();
        Conta conta = novaConta();
        UUID id = UUID.randomUUID();
        when(contaRepository.buscarPorId(id)).thenReturn(Optional.of(conta));

        Conta atualizada = contaService.atualizar(id, conta.getDescricao(), new BigDecimal("500.00"),
                conta.getDataVencimento(), fornecedor.getId());

        assertThat(atualizada.getValor()).isEqualByComparingTo("500.00");
    }

    @Test
    void naoDeveAtualizarValorQuandoNaoPendente() {
        Conta conta = novaConta();
        conta.cancelar();
        UUID id = UUID.randomUUID();
        when(contaRepository.buscarPorId(id)).thenReturn(Optional.of(conta));

        assertThatThrownBy(() -> contaService.atualizar(id, conta.getDescricao(), new BigDecimal("999.00"),
                conta.getDataVencimento(), fornecedor.getId()))
                .isInstanceOf(SituacaoInvalidaException.class);
    }

    @Test
    void deveTrocarFornecedorQuandoDiferente() {
        stubSalvarRetornandoOMesmo();
        Conta conta = novaConta();
        UUID id = UUID.randomUUID();
        Fornecedor novoFornecedor = new Fornecedor("Outro Fornecedor");
        UUID novoFornecedorId = UUID.randomUUID();
        ReflectionTestUtils.setField(novoFornecedor, "id", novoFornecedorId);
        when(contaRepository.buscarPorId(id)).thenReturn(Optional.of(conta));
        when(fornecedorRepository.buscarPorId(novoFornecedorId)).thenReturn(Optional.of(novoFornecedor));

        Conta atualizada = contaService.atualizar(id, conta.getDescricao(), conta.getValor(),
                conta.getDataVencimento(), novoFornecedorId);

        assertThat(atualizada.getFornecedor()).isEqualTo(novoFornecedor);
    }

    @Test
    void deveExcluirContaExistente() {
        Conta conta = novaConta();
        UUID id = UUID.randomUUID();
        when(contaRepository.buscarPorId(id)).thenReturn(Optional.of(conta));

        contaService.excluir(id);

        verify(contaRepository).excluir(conta);
    }

    @Test
    void naoDeveExcluirContaInexistente() {
        UUID id = UUID.randomUUID();
        when(contaRepository.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contaService.excluir(id)).isInstanceOf(ContaNaoEncontradaException.class);
        verify(contaRepository, never()).excluir(any());
    }

    @Test
    void devePagarContaViaAlterarSituacao() {
        stubSalvarRetornandoOMesmo();
        Conta conta = novaConta();
        UUID id = UUID.randomUUID();
        when(contaRepository.buscarPorId(id)).thenReturn(Optional.of(conta));

        Conta atualizada = contaService.alterarSituacao(id, Situacao.PAGO);

        assertThat(atualizada.getSituacao()).isEqualTo(Situacao.PAGO);
        assertThat(atualizada.getDataPagamento()).isEqualTo(LocalDate.now());
    }

    @Test
    void deveCancelarContaViaAlterarSituacao() {
        stubSalvarRetornandoOMesmo();
        Conta conta = novaConta();
        UUID id = UUID.randomUUID();
        when(contaRepository.buscarPorId(id)).thenReturn(Optional.of(conta));

        Conta atualizada = contaService.alterarSituacao(id, Situacao.CANCELADO);

        assertThat(atualizada.getSituacao()).isEqualTo(Situacao.CANCELADO);
    }

    @Test
    void naoDevePermitirAlterarSituacaoParaPendente() {
        Conta conta = novaConta();
        UUID id = UUID.randomUUID();
        when(contaRepository.buscarPorId(id)).thenReturn(Optional.of(conta));

        assertThatThrownBy(() -> contaService.alterarSituacao(id, Situacao.PENDENTE))
                .isInstanceOf(SituacaoInvalidaException.class);
    }

    @Test
    void devePesquisarDelegandoParaRepository() {
        Pageable pageable = Pageable.ofSize(10);
        Page<Conta> pagina = new PageImpl<>(java.util.List.of(novaConta()));
        PeriodoFiltro periodoVencimento = new PeriodoFiltro(null, null);
        when(contaRepository.pesquisar("Energia", periodoVencimento, pageable)).thenReturn(pagina);

        Page<Conta> resultado = contaService.pesquisar("Energia", periodoVencimento, pageable);

        assertThat(resultado).isSameAs(pagina);
    }

    @Test
    void deveRetornarTotalPagoDelegandoParaRepository() {
        PeriodoFiltro periodoPagamento = new PeriodoFiltro(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31));
        when(contaRepository.totalPago(periodoPagamento)).thenReturn(new BigDecimal("1500.00"));

        BigDecimal total = contaService.totalPago(periodoPagamento);

        assertThat(total).isEqualByComparingTo("1500.00");
    }

    @Test
    void naoDeveRetornarTotalPagoQuandoPeriodoIncompleto() {
        assertThatThrownBy(() -> contaService.totalPago(new PeriodoFiltro(null, LocalDate.of(2026, 8, 31))))
                .isInstanceOf(IntervaloDataInvalidoException.class);

        assertThatThrownBy(() -> contaService.totalPago(new PeriodoFiltro(LocalDate.of(2026, 8, 1), null)))
                .isInstanceOf(IntervaloDataInvalidoException.class);

        assertThatThrownBy(() -> contaService.totalPago(new PeriodoFiltro(null, null)))
                .isInstanceOf(IntervaloDataInvalidoException.class);

        verify(contaRepository, never()).totalPago(any());
    }

}
