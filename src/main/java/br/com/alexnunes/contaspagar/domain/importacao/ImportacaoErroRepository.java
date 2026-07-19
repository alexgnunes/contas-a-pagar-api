package br.com.alexnunes.contaspagar.domain.importacao;

import java.util.List;
import java.util.UUID;

public interface ImportacaoErroRepository {

    ImportacaoErro salvar(ImportacaoErro erro);

    List<ImportacaoErro> buscarPorImportacaoId(UUID importacaoId);

}
