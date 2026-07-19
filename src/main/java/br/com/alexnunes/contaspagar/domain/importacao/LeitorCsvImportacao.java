package br.com.alexnunes.contaspagar.domain.importacao;

import br.com.alexnunes.contaspagar.domain.importacao.exception.CsvInvalidoException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LeitorCsvImportacao {

    private static final String DELIMITADOR = ";";
    private static final List<String> CABECALHO_ESPERADO = List.of(
            "descricao", "valor", "dataVencimento", "dataPagamento", "situacao", "fornecedorId");

    public List<LinhaCsvImportacao> ler(InputStream conteudo) {
        try (BufferedReader leitor = new BufferedReader(new InputStreamReader(conteudo, StandardCharsets.UTF_8))) {
            String cabecalho = leitor.readLine();
            if (cabecalho == null || cabecalho.isBlank()) {
                throw new CsvInvalidoException("Arquivo CSV não possui cabeçalho");
            }

            validarCabecalho(cabecalho);

            return lerLinhas(leitor);
        } catch (IOException e) {
            throw new CsvInvalidoException("Falha ao ler arquivo CSV", e);
        }
    }

    private void validarCabecalho(String cabecalho) {
        String[] colunas = cabecalho.split(DELIMITADOR, -1);
        if (colunas.length < CABECALHO_ESPERADO.size()) {
            throw new CsvInvalidoException(
                    String.format("Cabeçalho do CSV não corresponde à ordem esperada: %s",
                            String.join(DELIMITADOR, CABECALHO_ESPERADO)));
        }
        for (int i = 0; i < CABECALHO_ESPERADO.size(); i++) {
            if (!CABECALHO_ESPERADO.get(i).equals(colunas[i].trim())) {
                throw new CsvInvalidoException(
                        String.format("Cabeçalho do CSV não corresponde à ordem esperada: %s",
                                String.join(DELIMITADOR, CABECALHO_ESPERADO)));
            }
        }
    }

    private List<LinhaCsvImportacao> lerLinhas(BufferedReader leitor) throws IOException {
        List<LinhaCsvImportacao> linhas = new ArrayList<>();
        String linha;
        int numeroLinha = 1;

        while ((linha = leitor.readLine()) != null) {
            numeroLinha++;
            if (linha.isBlank()) {
                continue;
            }
            linhas.add(interpretarLinha(linha, numeroLinha));
        }

        return linhas;
    }

    private LinhaCsvImportacao interpretarLinha(String linha, int numeroLinha) {
        String[] valores = linha.split(DELIMITADOR, -1);
        return new LinhaCsvImportacao(
                numeroLinha,
                valorDaColuna(valores, 0),
                valorDaColuna(valores, 1),
                valorDaColuna(valores, 2),
                valorDaColuna(valores, 3),
                valorDaColuna(valores, 4),
                valorDaColuna(valores, 5),
                linha);
    }

    private String valorDaColuna(String[] valores, int indice) {
        return indice < valores.length ? valores[indice].trim() : "";
    }

}
