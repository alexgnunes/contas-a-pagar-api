package br.com.alexnunes.contaspagar.controller.relatorio;

import br.com.alexnunes.contaspagar.application.conta.ContaService;
import br.com.alexnunes.contaspagar.controller.relatorio.dto.TotalPagoResponse;
import br.com.alexnunes.contaspagar.domain.conta.PeriodoFiltro;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/relatorios")
@Tag(name = "Relatórios")
public class RelatorioController {

    private final ContaService contaService;

    public RelatorioController(ContaService contaService) {
        this.contaService = contaService;
    }

    @GetMapping("/total-pago")
    @Operation(summary = "Calcula o total pago (soma de contas com situacao=PAGO) no período informado")
    @ApiResponse(responseCode = "200", description = "Total calculado com sucesso")
    @ApiResponse(responseCode = "400", description = "Parâmetro ausente ou período inválido (fim anterior a início)")
    public ResponseEntity<TotalPagoResponse> totalPago(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        PeriodoFiltro periodoPagamento = new PeriodoFiltro(inicio, fim);
        BigDecimal totalPago = contaService.totalPago(periodoPagamento);
        return ResponseEntity.ok(new TotalPagoResponse(inicio, fim, totalPago));
    }

}
