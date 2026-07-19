package br.com.alexnunes.contaspagar.domain.conta;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface ContaRepository {

    Conta salvar(Conta conta);

    Optional<Conta> buscarPorId(UUID id);

    void excluir(Conta conta);

    boolean existePorFornecedorId(UUID fornecedorId);

    Page<Conta> pesquisar(String descricao, LocalDate dataInicial, LocalDate dataFinal, Pageable pageable);

}
