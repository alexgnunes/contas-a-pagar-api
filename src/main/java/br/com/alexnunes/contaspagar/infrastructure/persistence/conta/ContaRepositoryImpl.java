package br.com.alexnunes.contaspagar.infrastructure.persistence.conta;

import br.com.alexnunes.contaspagar.domain.conta.Conta;
import br.com.alexnunes.contaspagar.domain.conta.ContaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
class ContaRepositoryImpl implements ContaRepository {

    private final ContaJpaRepository jpaRepository;

    ContaRepositoryImpl(ContaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Conta salvar(Conta conta) {
        return jpaRepository.save(conta);
    }

    @Override
    public Optional<Conta> buscarPorId(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public void excluir(Conta conta) {
        jpaRepository.delete(conta);
    }

    @Override
    public boolean existePorFornecedorId(UUID fornecedorId) {
        return jpaRepository.existsByFornecedorId(fornecedorId);
    }

}
