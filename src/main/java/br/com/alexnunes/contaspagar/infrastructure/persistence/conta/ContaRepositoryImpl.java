package br.com.alexnunes.contaspagar.infrastructure.persistence.conta;

import br.com.alexnunes.contaspagar.domain.conta.Conta;
import br.com.alexnunes.contaspagar.domain.conta.ContaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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
        return jpaRepository.buscarPorIdComFornecedor(id);
    }

    @Override
    public void excluir(Conta conta) {
        jpaRepository.delete(conta);
    }

    @Override
    public boolean existePorFornecedorId(UUID fornecedorId) {
        return jpaRepository.existsByFornecedorId(fornecedorId);
    }

    @Override
    public Page<Conta> pesquisar(String descricao, LocalDate dataInicial, LocalDate dataFinal, Pageable pageable) {
        return jpaRepository.pesquisar(escaparCoringasLike(descricao), dataInicial, dataFinal, pageable);
    }

    private static String escaparCoringasLike(String valor) {
        if (valor == null) {
            return null;
        }

        return valor
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

}
