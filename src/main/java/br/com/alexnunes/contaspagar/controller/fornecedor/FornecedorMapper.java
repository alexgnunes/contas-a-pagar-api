package br.com.alexnunes.contaspagar.controller.fornecedor;

import br.com.alexnunes.contaspagar.controller.fornecedor.dto.FornecedorResponse;
import br.com.alexnunes.contaspagar.domain.fornecedor.Fornecedor;
import org.springframework.stereotype.Component;

@Component
public class FornecedorMapper {

    public FornecedorResponse toResponse(Fornecedor fornecedor) {
        return new FornecedorResponse(fornecedor.getId(), fornecedor.getNome());
    }

}
