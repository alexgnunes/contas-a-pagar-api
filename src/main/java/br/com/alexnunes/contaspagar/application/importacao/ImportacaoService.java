package br.com.alexnunes.contaspagar.application.importacao;

import br.com.alexnunes.contaspagar.domain.importacao.ArmazenamentoArquivoImportacao;
import br.com.alexnunes.contaspagar.domain.importacao.GeradorCsvErros;
import br.com.alexnunes.contaspagar.domain.importacao.Importacao;
import br.com.alexnunes.contaspagar.domain.importacao.ImportacaoErro;
import br.com.alexnunes.contaspagar.domain.importacao.ImportacaoErroRepository;
import br.com.alexnunes.contaspagar.domain.importacao.ImportacaoRepository;
import br.com.alexnunes.contaspagar.domain.importacao.LeitorCsvImportacao;
import br.com.alexnunes.contaspagar.domain.importacao.LinhaCsvImportacao;
import br.com.alexnunes.contaspagar.domain.importacao.PublicadorImportacao;
import br.com.alexnunes.contaspagar.domain.importacao.enums.ImportacaoStatus;
import br.com.alexnunes.contaspagar.domain.importacao.exception.CsvInvalidoException;
import br.com.alexnunes.contaspagar.domain.importacao.exception.ImportacaoNaoEncontradaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
public class ImportacaoService {

    private static final Logger log = LoggerFactory.getLogger(ImportacaoService.class);
    private static final String MOTIVO_FALHA_GENERICO = "Falha inesperada ao processar a importação";

    private final ArmazenamentoArquivoImportacao armazenamentoArquivo;
    private final ImportacaoRepository importacaoRepository;
    private final ImportacaoErroRepository importacaoErroRepository;
    private final PublicadorImportacao publicadorImportacao;
    private final ContaImportacaoService contaImportacaoService;
    private final LeitorCsvImportacao leitor;
    private final GeradorCsvErros geradorCsvErros;

    public ImportacaoService(ArmazenamentoArquivoImportacao armazenamentoArquivo,
                              ImportacaoRepository importacaoRepository,
                              ImportacaoErroRepository importacaoErroRepository,
                              PublicadorImportacao publicadorImportacao,
                              ContaImportacaoService contaImportacaoService) {
        this.armazenamentoArquivo = armazenamentoArquivo;
        this.importacaoRepository = importacaoRepository;
        this.importacaoErroRepository = importacaoErroRepository;
        this.publicadorImportacao = publicadorImportacao;
        this.contaImportacaoService = contaImportacaoService;
        this.leitor = new LeitorCsvImportacao();
        this.geradorCsvErros = new GeradorCsvErros();
    }

    public String iniciar(InputStream conteudo, String nomeArquivo) {
        String caminhoArquivo = armazenamentoArquivo.salvar(conteudo, nomeArquivo);
        String protocolo = gerarProtocolo();

        importacaoRepository.salvar(new Importacao(protocolo, caminhoArquivo));
        publicadorImportacao.publicar(protocolo);

        return protocolo;
    }

    public void processarMensagem(String protocolo) {
        Importacao importacao = importacaoRepository.buscarPorProtocolo(protocolo).orElse(null);
        if (importacao == null) {
            log.error("Importação {} não encontrada, mensagem descartada", protocolo);
            return;
        }

        if (importacao.getStatus() != ImportacaoStatus.PROCESSANDO) {
            log.warn("Importação {} já está em status final ({}), mensagem redelivered descartada", protocolo,
                    importacao.getStatus());
            return;
        }

        try {
            processarLinhas(importacao, protocolo);
        } catch (RuntimeException e) {
            log.error("Falha inesperada ao processar importação {}, marcando como FALHOU", protocolo, e);
            importacao.falhar(MOTIVO_FALHA_GENERICO);
            importacaoRepository.salvar(importacao);
        }
    }

    private void processarLinhas(Importacao importacao, String protocolo) {
        List<LinhaCsvImportacao> linhas;
        try (InputStream conteudo = armazenamentoArquivo.abrir(importacao.getCaminhoArquivo())) {
            linhas = leitor.ler(conteudo);
        } catch (CsvInvalidoException e) {
            log.error("Falha estrutural ao processar importação {}", protocolo, e);
            importacao.falhar(e.getMessage());
            importacaoRepository.salvar(importacao);
            return;
        } catch (Exception e) {
            log.error("Falha estrutural ao processar importação {}", protocolo, e);
            importacao.falhar(MOTIVO_FALHA_GENERICO);
            importacaoRepository.salvar(importacao);
            return;
        }

        int sucesso = 0;
        int falhas = 0;
        for (LinhaCsvImportacao linha : linhas) {
            try {
                contaImportacaoService.processarLinha(linha);
                sucesso++;
            } catch (RuntimeException e) {
                falhas++;
                log.warn("Falha ao processar linha {} da importação {}: {}", linha.numeroLinha(), protocolo,
                        e.getMessage());
                importacaoErroRepository.salvar(
                        new ImportacaoErro(importacao, linha.numeroLinha(), e.getMessage(), linha.linhaOriginal()));
            }
        }

        importacao.concluir(linhas.size(), sucesso, falhas);
        importacaoRepository.salvar(importacao);
    }

    public Importacao consultarStatus(String protocolo) {
        return importacaoRepository.buscarPorProtocolo(protocolo)
                .orElseThrow(() -> new ImportacaoNaoEncontradaException(protocolo));
    }

    public String gerarCsvErros(String protocolo) {
        Importacao importacao = consultarStatus(protocolo);
        List<ImportacaoErro> erros = importacaoErroRepository.buscarPorImportacaoId(importacao.getId());
        return geradorCsvErros.gerar(erros);
    }

    private String gerarProtocolo() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }

}
