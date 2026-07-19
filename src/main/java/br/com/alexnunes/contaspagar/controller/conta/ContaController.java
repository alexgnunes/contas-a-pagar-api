package br.com.alexnunes.contaspagar.controller.conta;

import br.com.alexnunes.contaspagar.application.conta.ContaService;
import br.com.alexnunes.contaspagar.controller.conta.dto.AlterarSituacaoRequest;
import br.com.alexnunes.contaspagar.controller.conta.dto.ContaRequest;
import br.com.alexnunes.contaspagar.controller.conta.dto.ContaResponse;
import br.com.alexnunes.contaspagar.domain.conta.Conta;
import br.com.alexnunes.contaspagar.domain.conta.PeriodoFiltro;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/contas")
@Tag(name = "Contas")
public class ContaController {

    private final ContaService contaService;
    private final ContaMapper contaMapper;

    public ContaController(ContaService contaService, ContaMapper contaMapper) {
        this.contaService = contaService;
        this.contaMapper = contaMapper;
    }

    @PostMapping
    public ResponseEntity<ContaResponse> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
                    @ExampleObject(name = "Conta válida", value = ContaExemplos.CONTA_VALIDA),
                    @ExampleObject(name = "Valor negativo", value = ContaExemplos.VALOR_NEGATIVO),
                    @ExampleObject(name = "Fornecedor inexistente", value = ContaExemplos.FORNECEDOR_INEXISTENTE)
            }))
            @Valid @RequestBody ContaRequest request) {
        Conta conta = contaService.criar(request.descricao(), request.valor(), request.dataVencimento(),
                request.fornecedorId(), request.situacao(), request.dataPagamento());
        return ResponseEntity.created(URI.create(String.format("/contas/%s", conta.getId())))
                .body(contaMapper.toResponse(conta));
    }

    @GetMapping
    @Parameter(name = "sort", in = ParameterIn.QUERY, description = "Ordenação no formato propriedade,direção (asc|desc)",
            array = @ArraySchema(schema = @Schema(type = "string", example = "descricao,asc")))
    public ResponseEntity<Page<ContaResponse>> pesquisar(
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataVencimentoInicial,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataVencimentoFinal,
            @org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        PeriodoFiltro periodoVencimento = new PeriodoFiltro(dataVencimentoInicial, dataVencimentoFinal);
        Page<ContaResponse> pagina = contaService.pesquisar(descricao, periodoVencimento, pageable)
                .map(contaMapper::toResponse);
        return ResponseEntity.ok(pagina);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContaResponse> buscarPorId(@PathVariable UUID id) {
        Conta conta = contaService.buscarPorId(id);
        return ResponseEntity.ok(contaMapper.toResponse(conta));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContaResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody ContaRequest request) {
        Conta conta = contaService.atualizar(id, request.descricao(), request.valor(), request.dataVencimento(),
                request.fornecedorId());
        return ResponseEntity.ok(contaMapper.toResponse(conta));
    }

    @PatchMapping("/{id}/situacao")
    public ResponseEntity<ContaResponse> alterarSituacao(@PathVariable UUID id,
                                                           @Valid @RequestBody AlterarSituacaoRequest request) {
        Conta conta = contaService.alterarSituacao(id, request.situacao(), request.dataPagamento());
        return ResponseEntity.ok(contaMapper.toResponse(conta));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        contaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

}
