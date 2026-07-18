package br.com.alexnunes.contaspagar.application.fornecedor;

import br.com.alexnunes.contaspagar.domain.conta.ContaRepository;
import br.com.alexnunes.contaspagar.domain.fornecedor.Fornecedor;
import br.com.alexnunes.contaspagar.domain.fornecedor.FornecedorRepository;
import br.com.alexnunes.contaspagar.domain.fornecedor.exception.FornecedorComContasVinculadasException;
import br.com.alexnunes.contaspagar.domain.fornecedor.exception.FornecedorNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;
    private final ContaRepository contaRepository;

    public FornecedorService(FornecedorRepository fornecedorRepository, ContaRepository contaRepository) {
        this.fornecedorRepository = fornecedorRepository;
        this.contaRepository = contaRepository;
    }

    @Transactional
    public Fornecedor criar(String nome) {
        return fornecedorRepository.salvar(new Fornecedor(nome));
    }

    @Transactional(readOnly = true)
    public Fornecedor buscarPorId(UUID id) {
        return fornecedorRepository.buscarPorId(id)
                .orElseThrow(() -> new FornecedorNaoEncontradoException(id));
    }

    @Transactional(readOnly = true)
    public List<Fornecedor> listarTodos() {
        return fornecedorRepository.buscarTodos();
    }

    @Transactional
    public Fornecedor atualizar(UUID id, String novoNome) {
        Fornecedor fornecedor = buscarPorId(id);
        fornecedor.alterarNome(novoNome);
        return fornecedorRepository.salvar(fornecedor);
    }

    @Transactional
    public void excluir(UUID id) {
        Fornecedor fornecedor = buscarPorId(id);

        if (contaRepository.existePorFornecedorId(id)) {
            throw new FornecedorComContasVinculadasException(id);
        }

        fornecedorRepository.excluir(fornecedor);
    }

}
