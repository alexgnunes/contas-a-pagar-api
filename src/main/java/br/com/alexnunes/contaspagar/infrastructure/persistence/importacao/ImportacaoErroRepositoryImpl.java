package br.com.alexnunes.contaspagar.infrastructure.persistence.importacao;

import br.com.alexnunes.contaspagar.domain.importacao.ImportacaoErro;
import br.com.alexnunes.contaspagar.domain.importacao.ImportacaoErroRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
class ImportacaoErroRepositoryImpl implements ImportacaoErroRepository {

    private final ImportacaoErroJpaRepository jpaRepository;

    ImportacaoErroRepositoryImpl(ImportacaoErroJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ImportacaoErro salvar(ImportacaoErro erro) {
        return jpaRepository.save(erro);
    }

    @Override
    public List<ImportacaoErro> buscarPorImportacaoId(UUID importacaoId) {
        return jpaRepository.findByImportacao_IdOrderByLinha(importacaoId);
    }

}
