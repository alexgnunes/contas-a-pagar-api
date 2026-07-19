package br.com.alexnunes.contaspagar.controller.fornecedor;

import br.com.alexnunes.contaspagar.application.fornecedor.FornecedorService;
import br.com.alexnunes.contaspagar.controller.fornecedor.dto.FornecedorRequest;
import br.com.alexnunes.contaspagar.controller.fornecedor.dto.FornecedorResponse;
import br.com.alexnunes.contaspagar.domain.fornecedor.Fornecedor;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/fornecedores")
@Tag(name = "Fornecedores")
public class FornecedorController {

    private final FornecedorService fornecedorService;
    private final FornecedorMapper fornecedorMapper;

    public FornecedorController(FornecedorService fornecedorService, FornecedorMapper fornecedorMapper) {
        this.fornecedorService = fornecedorService;
        this.fornecedorMapper = fornecedorMapper;
    }

    @PostMapping
    public ResponseEntity<FornecedorResponse> criar(@Valid @RequestBody FornecedorRequest request) {
        Fornecedor fornecedor = fornecedorService.criar(request.nome());
        return ResponseEntity.created(URI.create(String.format("/fornecedores/%s", fornecedor.getId())))
                .body(fornecedorMapper.toResponse(fornecedor));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FornecedorResponse> buscarPorId(
            @Parameter(example = "11111111-1111-1111-1111-111111111111") @PathVariable UUID id) {
        Fornecedor fornecedor = fornecedorService.buscarPorId(id);
        return ResponseEntity.ok(fornecedorMapper.toResponse(fornecedor));
    }

    @GetMapping
    public ResponseEntity<List<FornecedorResponse>> listarTodos() {
        List<FornecedorResponse> resposta = fornecedorService.listarTodos().stream()
                .map(fornecedorMapper::toResponse)
                .toList();
        return ResponseEntity.ok(resposta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FornecedorResponse> atualizar(
            @Parameter(example = "11111111-1111-1111-1111-111111111111") @PathVariable UUID id,
            @Valid @RequestBody FornecedorRequest request) {
        Fornecedor fornecedor = fornecedorService.atualizar(id, request.nome());
        return ResponseEntity.ok(fornecedorMapper.toResponse(fornecedor));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @Parameter(example = "11111111-1111-1111-1111-111111111111") @PathVariable UUID id) {
        fornecedorService.excluir(id);
        return ResponseEntity.noContent().build();
    }

}
