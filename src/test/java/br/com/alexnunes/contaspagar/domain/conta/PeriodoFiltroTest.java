package br.com.alexnunes.contaspagar.domain.conta;

import br.com.alexnunes.contaspagar.domain.conta.exception.IntervaloDataInvalidoException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PeriodoFiltroTest {

    @Test
    void deveCriarQuandoFimPosteriorOuIgualAoInicio() {
        PeriodoFiltro periodo = new PeriodoFiltro(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31));

        assertThat(periodo.inicio()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(periodo.fim()).isEqualTo(LocalDate.of(2026, 8, 31));
    }

    @Test
    void deveCriarQuandoInicioEFimSaoIguais() {
        LocalDate data = LocalDate.of(2026, 8, 15);

        PeriodoFiltro periodo = new PeriodoFiltro(data, data);

        assertThat(periodo.inicio()).isEqualTo(data);
        assertThat(periodo.fim()).isEqualTo(data);
    }

    @Test
    void deveCriarQuandoInicioOuFimSaoNulos() {
        assertThat(new PeriodoFiltro(null, LocalDate.of(2026, 8, 31)).inicio()).isNull();
        assertThat(new PeriodoFiltro(LocalDate.of(2026, 8, 1), null).fim()).isNull();
        assertThat(new PeriodoFiltro(null, null)).isNotNull();
    }

    @Test
    void naoDeveCriarQuandoFimAnteriorAoInicio() {
        assertThatThrownBy(() -> new PeriodoFiltro(LocalDate.of(2026, 8, 31), LocalDate.of(2026, 8, 1)))
                .isInstanceOf(IntervaloDataInvalidoException.class);
    }

}
