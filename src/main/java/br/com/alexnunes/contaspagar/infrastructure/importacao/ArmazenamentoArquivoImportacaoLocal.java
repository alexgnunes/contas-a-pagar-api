package br.com.alexnunes.contaspagar.infrastructure.importacao;

import br.com.alexnunes.contaspagar.domain.importacao.ArmazenamentoArquivoImportacao;
import br.com.alexnunes.contaspagar.domain.importacao.exception.ArmazenamentoArquivoException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Component
class ArmazenamentoArquivoImportacaoLocal implements ArmazenamentoArquivoImportacao {

    private final Path diretorioImportacoes;

    ArmazenamentoArquivoImportacaoLocal(ImportacaoProperties importacaoProperties) {
        this.diretorioImportacoes = Path.of(importacaoProperties.diretorio());
    }

    @Override
    public String salvar(InputStream conteudo, String nomeArquivo) {
        try (conteudo) {
            Files.createDirectories(diretorioImportacoes);
            Path arquivoTemporario = Files.createTempFile(diretorioImportacoes, "importacao-", ".csv");
            Files.copy(conteudo, arquivoTemporario, StandardCopyOption.REPLACE_EXISTING);
            return arquivoTemporario.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new ArmazenamentoArquivoException(
                    String.format("Falha ao salvar arquivo temporário: %s", nomeArquivo), e);
        }
    }

    @Override
    public InputStream abrir(String caminhoArquivo) {
        try {
            return Files.newInputStream(Path.of(caminhoArquivo));
        } catch (IOException e) {
            throw new ArmazenamentoArquivoException(
                    String.format("Falha ao abrir arquivo: %s", caminhoArquivo), e);
        }
    }

}
