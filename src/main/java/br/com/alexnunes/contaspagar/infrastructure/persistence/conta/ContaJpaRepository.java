package br.com.alexnunes.contaspagar.infrastructure.persistence.conta;

import br.com.alexnunes.contaspagar.domain.conta.Conta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface ContaJpaRepository extends JpaRepository<Conta, UUID> {

    boolean existsByFornecedorId(UUID fornecedorId);

}
