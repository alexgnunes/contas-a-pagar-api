package br.com.alexnunes.contaspagar.infrastructure.persistence.importacao;

import br.com.alexnunes.contaspagar.domain.importacao.Importacao;
import br.com.alexnunes.contaspagar.domain.importacao.ImportacaoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class ImportacaoRepositoryImpl implements ImportacaoRepository {

    private final ImportacaoJpaRepository jpaRepository;

    ImportacaoRepositoryImpl(ImportacaoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Importacao salvar(Importacao importacao) {
        return jpaRepository.save(importacao);
    }

    @Override
    public Optional<Importacao> buscarPorProtocolo(String protocolo) {
        return jpaRepository.findByProtocolo(protocolo);
    }

}
