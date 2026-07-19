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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class ContaService {

    private final ContaRepository contaRepository;
    private final FornecedorRepository fornecedorRepository;

    public ContaService(ContaRepository contaRepository, FornecedorRepository fornecedorRepository) {
        this.contaRepository = contaRepository;
        this.fornecedorRepository = fornecedorRepository;
    }

    @Transactional
    public Conta criar(String descricao, BigDecimal valor, LocalDate dataVencimento, UUID fornecedorId) {
        Fornecedor fornecedor = buscarFornecedor(fornecedorId);
        Conta conta = Conta.criarPendente(descricao, valor, dataVencimento, fornecedor);
        return contaRepository.salvar(conta);
    }

    @Transactional
    public Conta criar(String descricao, BigDecimal valor, LocalDate dataVencimento, UUID fornecedorId,
                        Situacao situacao, LocalDate dataPagamento) {
        Fornecedor fornecedor = buscarFornecedor(fornecedorId);

        Conta conta = switch (situacao) {
            case PENDENTE -> Conta.criarPendente(descricao, valor, dataVencimento, fornecedor);
            case PAGO -> Conta.criarPaga(descricao, valor, dataVencimento, dataPagamento, fornecedor);
            case CANCELADO -> Conta.criarCancelada(descricao, valor, dataVencimento, fornecedor);
        };

        return contaRepository.salvar(conta);
    }

    @Transactional(readOnly = true)
    public Conta buscarPorId(UUID id) {
        return contaRepository.buscarPorId(id)
                .orElseThrow(() -> new ContaNaoEncontradaException(id));
    }

    @Transactional
    public Conta atualizar(UUID id, String descricao, BigDecimal valor, LocalDate dataVencimento, UUID fornecedorId) {
        Conta conta = buscarPorId(id);

        conta.alterarDescricao(descricao);
        conta.alterarDataVencimento(dataVencimento);
        conta.alterarValor(valor);

        if (!conta.getFornecedor().getId().equals(fornecedorId)) {
            conta.trocarFornecedor(buscarFornecedor(fornecedorId));
        }

        return contaRepository.salvar(conta);
    }

    @Transactional
    public void excluir(UUID id) {
        contaRepository.excluir(buscarPorId(id));
    }

    @Transactional
    public Conta alterarSituacao(UUID id, Situacao novaSituacao) {
        Conta conta = buscarPorId(id);

        switch (novaSituacao) {
            case PAGO -> conta.pagar();
            case CANCELADO -> conta.cancelar();
            case PENDENTE -> throw new SituacaoInvalidaException(
                    "Não é possível definir a situação como pendente diretamente");
        }

        return contaRepository.salvar(conta);
    }

    @Transactional(readOnly = true)
    public Page<Conta> pesquisar(String descricao, PeriodoFiltro periodoVencimento, Pageable pageable) {
        return contaRepository.pesquisar(descricao, periodoVencimento, pageable);
    }

    @Transactional(readOnly = true)
    public BigDecimal totalPago(PeriodoFiltro periodoPagamento) {
        if (periodoPagamento.inicio() == null || periodoPagamento.fim() == null) {
            throw new IntervaloDataInvalidoException(
                    "Período (início e fim) é obrigatório para o relatório de total pago");
        }

        return contaRepository.totalPago(periodoPagamento);
    }

    private Fornecedor buscarFornecedor(UUID fornecedorId) {
        return fornecedorRepository.buscarPorId(fornecedorId)
                .orElseThrow(() -> new FornecedorNaoEncontradoException(fornecedorId));
    }

}
