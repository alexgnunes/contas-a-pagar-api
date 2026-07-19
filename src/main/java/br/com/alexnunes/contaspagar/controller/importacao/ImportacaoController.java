package br.com.alexnunes.contaspagar.controller.importacao;

import br.com.alexnunes.contaspagar.application.importacao.ImportacaoService;
import br.com.alexnunes.contaspagar.controller.importacao.dto.ImportacaoResponse;
import br.com.alexnunes.contaspagar.domain.importacao.exception.ArmazenamentoArquivoException;
import br.com.alexnunes.contaspagar.domain.importacao.exception.ArquivoVazioException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/importacoes")
@Tag(name = "Importações")
public class ImportacaoController {

    private final ImportacaoService importacaoService;

    public ImportacaoController(ImportacaoService importacaoService) {
        this.importacaoService = importacaoService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportacaoResponse> importar(@RequestParam("arquivo") MultipartFile arquivo) {
        validarArquivo(arquivo);
        String protocolo = importacaoService.iniciar(lerConteudo(arquivo), arquivo.getOriginalFilename());
        return ResponseEntity.accepted().body(new ImportacaoResponse(protocolo));
    }

    private void validarArquivo(MultipartFile arquivo) {
        if (arquivo.isEmpty()) {
            throw new ArquivoVazioException("Arquivo CSV não pode estar vazio");
        }
    }

    private InputStream lerConteudo(MultipartFile arquivo) {
        try {
            return arquivo.getInputStream();
        } catch (IOException e) {
            throw new ArmazenamentoArquivoException(
                    String.format("Falha ao ler arquivo enviado: %s", arquivo.getOriginalFilename()), e);
        }
    }

}
