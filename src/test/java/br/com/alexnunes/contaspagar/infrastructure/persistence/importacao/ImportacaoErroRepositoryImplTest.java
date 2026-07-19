package br.com.alexnunes.contaspagar.infrastructure.persistence.importacao;

import br.com.alexnunes.contaspagar.domain.importacao.Importacao;
import br.com.alexnunes.contaspagar.domain.importacao.ImportacaoErro;
import br.com.alexnunes.contaspagar.domain.importacao.ImportacaoErroRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(ImportacaoErroRepositoryImpl.class)
class ImportacaoErroRepositoryImplTest {

    private final ImportacaoErroRepository importacaoErroRepository;
    private final TestEntityManager entityManager;

    ImportacaoErroRepositoryImplTest(ImportacaoErroRepository importacaoErroRepository,
                                      TestEntityManager entityManager) {
        this.importacaoErroRepository = importacaoErroRepository;
        this.entityManager = entityManager;
    }

    @Test
    void deveSalvarEBuscarPorImportacaoIdOrdenadoPorLinha() {
        Importacao importacao = entityManager.persist(new Importacao("ABC123", "/dados/arquivo.csv"));

        importacaoErroRepository.salvar(new ImportacaoErro(importacao, 4, "Valor deve ser positivo",
                "Internet;-50;2026-08-15;;PENDENTE;2"));
        importacaoErroRepository.salvar(new ImportacaoErro(importacao, 2, "Fornecedor inexistente",
                "Energia;350;2026-08-10;;PENDENTE;99"));

        var erros = importacaoErroRepository.buscarPorImportacaoId(importacao.getId());

        assertThat(erros).hasSize(2);
        assertThat(erros.get(0).getLinha()).isEqualTo(2);
        assertThat(erros.get(0).getMensagem()).isEqualTo("Fornecedor inexistente");
        assertThat(erros.get(1).getLinha()).isEqualTo(4);
    }

    @Test
    void deveRetornarListaVaziaQuandoImportacaoNaoTemErros() {
        Importacao importacao = entityManager.persist(new Importacao("SEMERRO1", "/dados/outro.csv"));

        assertThat(importacaoErroRepository.buscarPorImportacaoId(importacao.getId())).isEmpty();
    }

    @Test
    void deveRetornarListaVaziaParaIdInexistente() {
        assertThat(importacaoErroRepository.buscarPorImportacaoId(UUID.randomUUID())).isEmpty();
    }

}
