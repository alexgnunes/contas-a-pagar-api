package br.com.alexnunes.contaspagar.infrastructure.persistence.importacao;

import br.com.alexnunes.contaspagar.domain.importacao.ImportacaoErro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface ImportacaoErroJpaRepository extends JpaRepository<ImportacaoErro, UUID> {

    List<ImportacaoErro> findByImportacao_IdOrderByLinha(UUID importacaoId);

}
