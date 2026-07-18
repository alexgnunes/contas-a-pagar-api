package br.com.alexnunes.contaspagar.infrastructure.persistence.fornecedor;

import br.com.alexnunes.contaspagar.domain.fornecedor.Fornecedor;
import br.com.alexnunes.contaspagar.domain.fornecedor.FornecedorRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class FornecedorRepositoryImpl implements FornecedorRepository {

    private final FornecedorJpaRepository jpaRepository;

    FornecedorRepositoryImpl(FornecedorJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Fornecedor salvar(Fornecedor fornecedor) {
        return jpaRepository.save(fornecedor);
    }

    @Override
    public Optional<Fornecedor> buscarPorId(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Fornecedor> buscarTodos() {
        return jpaRepository.findAll();
    }

    @Override
    public boolean existePorId(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public void excluir(Fornecedor fornecedor) {
        jpaRepository.delete(fornecedor);
    }

}
