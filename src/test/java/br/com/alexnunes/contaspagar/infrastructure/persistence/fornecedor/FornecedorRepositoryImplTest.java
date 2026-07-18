package br.com.alexnunes.contaspagar.infrastructure.persistence.fornecedor;

import br.com.alexnunes.contaspagar.domain.fornecedor.Fornecedor;
import br.com.alexnunes.contaspagar.domain.fornecedor.FornecedorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(FornecedorRepositoryImpl.class)
class FornecedorRepositoryImplTest {

    private final FornecedorRepository fornecedorRepository;

    FornecedorRepositoryImplTest(FornecedorRepository fornecedorRepository) {
        this.fornecedorRepository = fornecedorRepository;
    }

    @Test
    void deveSalvarEBuscarPorId() {
        Fornecedor salvo = fornecedorRepository.salvar(new Fornecedor("Fornecedor Teste"));

        assertThat(salvo.getId()).isNotNull();
        assertThat(fornecedorRepository.buscarPorId(salvo.getId()))
                .isPresent()
                .get()
                .extracting(Fornecedor::getNome)
                .isEqualTo("Fornecedor Teste");
    }

    @Test
    void deveRetornarVazioQuandoIdNaoExiste() {
        assertThat(fornecedorRepository.buscarPorId(UUID.randomUUID())).isEmpty();
    }

    @Test
    void deveListarTodos() {
        fornecedorRepository.salvar(new Fornecedor("Fornecedor A"));
        fornecedorRepository.salvar(new Fornecedor("Fornecedor B"));

        assertThat(fornecedorRepository.buscarTodos()).hasSize(2);
    }

    @Test
    void deveInformarSeExistePorId() {
        Fornecedor salvo = fornecedorRepository.salvar(new Fornecedor("Fornecedor Teste"));

        assertThat(fornecedorRepository.existePorId(salvo.getId())).isTrue();
        assertThat(fornecedorRepository.existePorId(UUID.randomUUID())).isFalse();
    }

    @Test
    void deveExcluir() {
        Fornecedor salvo = fornecedorRepository.salvar(new Fornecedor("Fornecedor Teste"));

        fornecedorRepository.excluir(salvo);

        assertThat(fornecedorRepository.buscarPorId(salvo.getId())).isEmpty();
    }

}
