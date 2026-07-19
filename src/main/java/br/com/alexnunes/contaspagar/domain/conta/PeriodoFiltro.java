package br.com.alexnunes.contaspagar.domain.conta;

import br.com.alexnunes.contaspagar.domain.conta.exception.IntervaloDataInvalidoException;

import java.time.LocalDate;

public record PeriodoFiltro(LocalDate inicio, LocalDate fim) {

    public PeriodoFiltro {
        if (inicio != null && fim != null && fim.isBefore(inicio)) {
            throw new IntervaloDataInvalidoException("Data final não pode ser anterior à data inicial");
        }
    }

}
