package br.com.alexnunes.contaspagar.domain.importacao;

public sealed interface ResultadoValidacaoLinha permits LinhaImportacaoValida, LinhaImportacaoInvalida {

    int numeroLinha();

}
