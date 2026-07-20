package br.com.alexnunes.contaspagar.application.fornecedor;

import br.com.alexnunes.contaspagar.domain.conta.ContaRepository;
import br.com.alexnunes.contaspagar.domain.fornecedor.Fornecedor;
import br.com.alexnunes.contaspagar.domain.fornecedor.FornecedorRepository;
import br.com.alexnunes.contaspagar.domain.fornecedor.exception.FornecedorComContasVinculadasException;
import br.com.alexnunes.contaspagar.domain.fornecedor.exception.FornecedorNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FornecedorServiceTest {

    @Mock
    private FornecedorRepository fornecedorRepository;

    @Mock
    private ContaRepository contaRepository;

    private FornecedorService fornecedorService;

    @BeforeEach
    void setUp() {
        fornecedorService = new FornecedorService(fornecedorRepository, contaRepository);
    }

    private void stubSalvarRetornandoOMesmo() {
        when(fornecedorRepository.salvar(any(Fornecedor.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Fornecedor novoFornecedor(String nome) {
        Fornecedor fornecedor = new Fornecedor(nome);
        ReflectionTestUtils.setField(fornecedor, "id", UUID.randomUUID());
        return fornecedor;
    }

    @Test
    void deveCriarFornecedor() {
        stubSalvarRetornandoOMesmo();

        Fornecedor fornecedor = fornecedorService.criar("Fornecedor Alpha");

        assertThat(fornecedor.getNome()).isEqualTo("Fornecedor Alpha");
        verify(fornecedorRepository).salvar(any(Fornecedor.class));
    }

    @Test
    void deveBuscarPorIdQuandoExiste() {
        Fornecedor fornecedor = novoFornecedor("Fornecedor Alpha");
        when(fornecedorRepository.buscarPorId(fornecedor.getId())).thenReturn(Optional.of(fornecedor));

        assertThat(fornecedorService.buscarPorId(fornecedor.getId())).isEqualTo(fornecedor);
    }

    @Test
    void naoDeveBuscarPorIdQuandoNaoExiste() {
        UUID id = UUID.randomUUID();
        when(fornecedorRepository.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fornecedorService.buscarPorId(id))
                .isInstanceOf(FornecedorNaoEncontradoException.class);
    }

    @Test
    void deveListarTodosDelegandoParaRepository() {
        List<Fornecedor> fornecedores = List.of(novoFornecedor("Alpha"), novoFornecedor("Beta"));
        when(fornecedorRepository.buscarTodos()).thenReturn(fornecedores);

        assertThat(fornecedorService.listarTodos()).isEqualTo(fornecedores);
    }

    @Test
    void deveAtualizarNomeQuandoFornecedorExiste() {
        Fornecedor fornecedor = novoFornecedor("Nome Antigo");
        when(fornecedorRepository.buscarPorId(fornecedor.getId())).thenReturn(Optional.of(fornecedor));
        stubSalvarRetornandoOMesmo();

        Fornecedor atualizado = fornecedorService.atualizar(fornecedor.getId(), "Nome Novo");

        assertThat(atualizado.getNome()).isEqualTo("Nome Novo");
        verify(fornecedorRepository).salvar(fornecedor);
    }

    @Test
    void naoDeveAtualizarQuandoFornecedorNaoExiste() {
        UUID id = UUID.randomUUID();
        when(fornecedorRepository.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fornecedorService.atualizar(id, "Nome Novo"))
                .isInstanceOf(FornecedorNaoEncontradoException.class);
        verify(fornecedorRepository, never()).salvar(any());
    }

    @Test
    void deveExcluirQuandoNaoHaContaVinculada() {
        Fornecedor fornecedor = novoFornecedor("Fornecedor Alpha");
        when(fornecedorRepository.buscarPorId(fornecedor.getId())).thenReturn(Optional.of(fornecedor));
        when(contaRepository.existePorFornecedorId(fornecedor.getId())).thenReturn(false);

        fornecedorService.excluir(fornecedor.getId());

        verify(fornecedorRepository).excluir(fornecedor);
    }

    @Test
    void naoDeveExcluirQuandoHaContaVinculada() {
        Fornecedor fornecedor = novoFornecedor("Fornecedor Alpha");
        when(fornecedorRepository.buscarPorId(fornecedor.getId())).thenReturn(Optional.of(fornecedor));
        when(contaRepository.existePorFornecedorId(fornecedor.getId())).thenReturn(true);

        assertThatThrownBy(() -> fornecedorService.excluir(fornecedor.getId()))
                .isInstanceOf(FornecedorComContasVinculadasException.class);
        verify(fornecedorRepository, never()).excluir(any());
    }

    @Test
    void naoDeveExcluirQuandoFornecedorNaoExiste() {
        UUID id = UUID.randomUUID();
        when(fornecedorRepository.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fornecedorService.excluir(id))
                .isInstanceOf(FornecedorNaoEncontradoException.class);
        verify(contaRepository, never()).existePorFornecedorId(any());
        verify(fornecedorRepository, never()).excluir(any());
    }

}
