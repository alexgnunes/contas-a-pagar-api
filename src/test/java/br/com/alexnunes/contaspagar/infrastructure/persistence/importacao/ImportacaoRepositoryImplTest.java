package br.com.alexnunes.contaspagar.infrastructure.persistence.importacao;

import br.com.alexnunes.contaspagar.domain.importacao.Importacao;
import br.com.alexnunes.contaspagar.domain.importacao.ImportacaoRepository;
import br.com.alexnunes.contaspagar.domain.importacao.enums.ImportacaoStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(ImportacaoRepositoryImpl.class)
class ImportacaoRepositoryImplTest {

    private final ImportacaoRepository importacaoRepository;

    ImportacaoRepositoryImplTest(ImportacaoRepository importacaoRepository) {
        this.importacaoRepository = importacaoRepository;
    }

    @Test
    void deveSalvarEBuscarPorProtocolo() {
        Importacao salva = importacaoRepository.salvar(new Importacao("ABC123", "/dados/arquivo.csv"));

        assertThat(salva.getId()).isNotNull();
        assertThat(importacaoRepository.buscarPorProtocolo("ABC123"))
                .isPresent()
                .get()
                .satisfies(importacao -> {
                    assertThat(importacao.getCaminhoArquivo()).isEqualTo("/dados/arquivo.csv");
                    assertThat(importacao.getStatus()).isEqualTo(ImportacaoStatus.PROCESSANDO);
                });
    }

    @Test
    void deveRetornarVazioQuandoProtocoloNaoExiste() {
        assertThat(importacaoRepository.buscarPorProtocolo("INEXISTENTE")).isEmpty();
    }

    @Test
    void devePersistirStatusAposConcluir() {
        Importacao importacao = importacaoRepository.salvar(new Importacao("XYZ789", "/dados/outro.csv"));

        importacao.concluir(5, 3, 2);
        importacaoRepository.salvar(importacao);

        Importacao recarregada = importacaoRepository.buscarPorProtocolo("XYZ789").orElseThrow();
        assertThat(recarregada.getStatus()).isEqualTo(ImportacaoStatus.CONCLUIDO_COM_ERROS);
        assertThat(recarregada.getTotalRegistros()).isEqualTo(5);
        assertThat(recarregada.getSucesso()).isEqualTo(3);
        assertThat(recarregada.getFalhas()).isEqualTo(2);
    }

}
