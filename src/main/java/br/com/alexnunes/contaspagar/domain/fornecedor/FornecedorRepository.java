package br.com.alexnunes.contaspagar.domain.fornecedor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FornecedorRepository {

    Fornecedor salvar(Fornecedor fornecedor);

    Optional<Fornecedor> buscarPorId(UUID id);

    List<Fornecedor> buscarTodos();

    boolean existePorId(UUID id);

    void excluir(Fornecedor fornecedor);

}
