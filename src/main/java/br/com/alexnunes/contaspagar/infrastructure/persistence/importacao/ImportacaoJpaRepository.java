package br.com.alexnunes.contaspagar.infrastructure.persistence.importacao;

import br.com.alexnunes.contaspagar.domain.importacao.Importacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface ImportacaoJpaRepository extends JpaRepository<Importacao, UUID> {

    Optional<Importacao> findByProtocolo(String protocolo);

}
