package br.com.alexnunes.contaspagar.controller.relatorio.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TotalPagoResponse(LocalDate periodoInicio, LocalDate periodoFim, BigDecimal totalPago) {
}
