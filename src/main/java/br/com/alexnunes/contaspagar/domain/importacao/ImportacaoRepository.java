package br.com.alexnunes.contaspagar.domain.importacao;

import java.util.Optional;

public interface ImportacaoRepository {

    Importacao salvar(Importacao importacao);

    Optional<Importacao> buscarPorProtocolo(String protocolo);

}
