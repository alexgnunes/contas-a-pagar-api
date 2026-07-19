package br.com.alexnunes.contaspagar.infrastructure.persistence.conta;

import br.com.alexnunes.contaspagar.domain.conta.Conta;
import br.com.alexnunes.contaspagar.domain.conta.enums.Situacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

interface ContaJpaRepository extends JpaRepository<Conta, UUID> {

    String WHERE_PESQUISA = """
            WHERE (:descricao IS NULL OR LOWER(c.descricao) LIKE LOWER(CONCAT('%', CAST(:descricao AS string), '%')) ESCAPE '\\')
            AND (CAST(:dataInicial AS date) IS NULL OR c.dataVencimento >= :dataInicial)
            AND (CAST(:dataFinal AS date) IS NULL OR c.dataVencimento <= :dataFinal)
            """;

    @Query("SELECT c FROM Conta c JOIN FETCH c.fornecedor WHERE c.id = :id")
    Optional<Conta> buscarPorIdComFornecedor(UUID id);

    @Query(value = "SELECT c FROM Conta c JOIN FETCH c.fornecedor " + WHERE_PESQUISA,
            countQuery = "SELECT COUNT(c) FROM Conta c " + WHERE_PESQUISA)
    Page<Conta> pesquisar(@Param("descricao") String descricao,
                          @Param("dataInicial") LocalDate dataInicial,
                          @Param("dataFinal") LocalDate dataFinal,
                          Pageable pageable);

    boolean existsByFornecedorId(UUID fornecedorId);

    @Query("""
            SELECT COALESCE(SUM(c.valor), 0) FROM Conta c
            WHERE c.situacao = :situacao AND c.dataPagamento BETWEEN :inicio AND :fim
            """)
    BigDecimal totalPago(@Param("situacao") Situacao situacao,
                         @Param("inicio") LocalDate inicio,
                         @Param("fim") LocalDate fim);

}
