package br.com.alexnunes.contaspagar.domain.importacao;

import java.io.InputStream;

public interface ArmazenamentoArquivoImportacao {

    String salvar(InputStream conteudo, String nomeArquivo);

    InputStream abrir(String caminhoArquivo);

}
