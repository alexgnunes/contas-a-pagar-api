package br.com.alexnunes.contaspagar.controller.conta;

import br.com.alexnunes.contaspagar.controller.conta.dto.ContaResponse;
import br.com.alexnunes.contaspagar.controller.fornecedor.FornecedorMapper;
import br.com.alexnunes.contaspagar.domain.conta.Conta;
import org.springframework.stereotype.Component;

@Component
public class ContaMapper {

    private final FornecedorMapper fornecedorMapper;

    public ContaMapper(FornecedorMapper fornecedorMapper) {
        this.fornecedorMapper = fornecedorMapper;
    }

    public ContaResponse toResponse(Conta conta) {
        return new ContaResponse(
                conta.getId(),
                conta.getDescricao(),
                conta.getValor(),
                conta.getDataVencimento(),
                conta.getDataPagamento(),
                conta.getSituacao(),
                fornecedorMapper.toResponse(conta.getFornecedor()));
    }

}
