package br.com.alexnunes.contaspagar.application.importacao;

import br.com.alexnunes.contaspagar.domain.importacao.ArmazenamentoArquivoImportacao;
import br.com.alexnunes.contaspagar.domain.importacao.Importacao;
import br.com.alexnunes.contaspagar.domain.importacao.ImportacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.UUID;

@Service
public class ImportacaoService {

    private final ArmazenamentoArquivoImportacao armazenamentoArquivo;
    private final ImportacaoRepository importacaoRepository;

    public ImportacaoService(ArmazenamentoArquivoImportacao armazenamentoArquivo,
                              ImportacaoRepository importacaoRepository) {
        this.armazenamentoArquivo = armazenamentoArquivo;
        this.importacaoRepository = importacaoRepository;
    }

    @Transactional
    public String iniciar(InputStream conteudo, String nomeArquivo) {
        String caminhoArquivo = armazenamentoArquivo.salvar(conteudo, nomeArquivo);
        String protocolo = gerarProtocolo();

        importacaoRepository.salvar(new Importacao(protocolo, caminhoArquivo));

        return protocolo;
    }

    private String gerarProtocolo() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }

}
