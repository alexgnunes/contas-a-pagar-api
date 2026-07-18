package br.com.alexnunes.contaspagar.infrastructure.persistence.fornecedor;

import br.com.alexnunes.contaspagar.domain.fornecedor.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface FornecedorJpaRepository extends JpaRepository<Fornecedor, UUID> {
}
