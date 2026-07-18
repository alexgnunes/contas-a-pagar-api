package br.com.alexnunes.contaspagar.infrastructure.persistence.conta;

import br.com.alexnunes.contaspagar.domain.conta.Conta;
import br.com.alexnunes.contaspagar.domain.conta.ContaRepository;
import br.com.alexnunes.contaspagar.domain.conta.enums.Situacao;
import br.com.alexnunes.contaspagar.domain.fornecedor.Fornecedor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(ContaRepositoryImpl.class)
class ContaRepositoryImplTest {

    private final ContaRepository contaRepository;
    private final TestEntityManager entityManager;

    ContaRepositoryImplTest(ContaRepository contaRepository, TestEntityManager entityManager) {
        this.contaRepository = contaRepository;
        this.entityManager = entityManager;
    }

    @Test
    void deveSalvarEBuscarPorId() {
        Fornecedor fornecedor = entityManager.persist(new Fornecedor("Fornecedor Teste"));
        Conta conta = new Conta("Energia", new BigDecimal("350.00"), LocalDate.of(2026, 8, 10), fornecedor);

        Conta salva = contaRepository.salvar(conta);

        assertThat(salva.getId()).isNotNull();

        Conta encontrada = contaRepository.buscarPorId(salva.getId()).orElseThrow();
        assertThat(encontrada.getDescricao()).isEqualTo("Energia");
        assertThat(encontrada.getValor()).isEqualByComparingTo("350.00");
        assertThat(encontrada.getSituacao()).isEqualTo(Situacao.PENDENTE);
        assertThat(encontrada.getDataPagamento()).isNull();
        assertThat(encontrada.getFornecedor().getId()).isEqualTo(fornecedor.getId());
    }

    @Test
    void deveRetornarVazioQuandoIdNaoExiste() {
        assertThat(contaRepository.buscarPorId(UUID.randomUUID())).isEmpty();
    }

    @Test
    void deveExcluir() {
        Fornecedor fornecedor = entityManager.persist(new Fornecedor("Fornecedor Teste"));
        Conta conta = contaRepository.salvar(
                new Conta("Internet", new BigDecimal("120.50"), LocalDate.of(2026, 8, 15), fornecedor));

        contaRepository.excluir(conta);

        assertThat(contaRepository.buscarPorId(conta.getId())).isEmpty();
    }

}
