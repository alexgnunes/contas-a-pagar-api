package br.com.alexnunes.contaspagar.domain.importacao;

public record LinhaImportacaoInvalida(int numeroLinha, String mensagem) implements ResultadoValidacaoLinha {
}
