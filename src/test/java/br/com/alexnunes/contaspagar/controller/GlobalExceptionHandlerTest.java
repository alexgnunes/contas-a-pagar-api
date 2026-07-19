package br.com.alexnunes.contaspagar.controller;

import br.com.alexnunes.contaspagar.application.conta.ContaService;
import br.com.alexnunes.contaspagar.controller.conta.ContaController;
import br.com.alexnunes.contaspagar.controller.conta.ContaMapper;
import br.com.alexnunes.contaspagar.domain.conta.exception.DataPagamentoInvalidaException;
import br.com.alexnunes.contaspagar.infrastructure.security.JwtService;
import org.hibernate.query.sqm.UnknownPathException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ContaController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContaService contaService;

    @MockBean
    private ContaMapper contaMapper;

    @MockBean
    private JwtService jwtService;

    @Test
    void deveRetornar400ComCamposQuandoBeanValidationFalha() throws Exception {
        String corpoInvalido = """
                {
                  "descricao": "",
                  "valor": -10,
                  "dataVencimento": "2026-08-10",
                  "fornecedorId": "11111111-1111-1111-1111-111111111111"
                }""";

        mockMvc.perform(post("/contas").contentType("application/json").content(corpoInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("descricao")))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("valor")));
    }

    @Test
    void deveRetornar400QuandoSortReferenciaPropriedadeInexistente() throws Exception {
        InvalidDataAccessApiUsageException excecao = new InvalidDataAccessApiUsageException(
                "could not prepare statement",
                new IllegalArgumentException("erro ao criar query",
                        new UnknownPathException("Could not resolve attribute 'string' of 'Conta'")));
        when(contaService.pesquisar(any(), any(), any())).thenThrow(excecao);

        mockMvc.perform(get("/contas").param("sort", "string"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("sort")));
    }

    @Test
    void deveRetornar500QuandoInvalidDataAccessApiUsageExceptionNaoEhSobreSort() throws Exception {
        when(contaService.pesquisar(any(), any(), any()))
                .thenThrow(new InvalidDataAccessApiUsageException("erro genérico de acesso a dados"));

        mockMvc.perform(get("/contas"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    void deveRetornar400QuandoDataPagamentoInvalida() throws Exception {
        when(contaService.criar(anyString(), any(), any(), any(), any(), any()))
                .thenThrow(new DataPagamentoInvalidaException("dataPagamento deve estar vazia quando situacao=PENDENTE"));

        String corpo = """
                {
                  "descricao": "Energia",
                  "valor": 350.00,
                  "dataVencimento": "2026-08-10",
                  "fornecedorId": "11111111-1111-1111-1111-111111111111",
                  "situacao": "PENDENTE",
                  "dataPagamento": "2026-01-01"
                }""";

        mockMvc.perform(post("/contas").contentType("application/json").content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("dataPagamento deve estar vazia quando situacao=PENDENTE"));
    }

    @Test
    void deveRetornar400QuandoEnumInvalidoNoCorpoJson() throws Exception {
        String corpoComSituacaoInvalida = """
                {
                  "descricao": "Energia",
                  "valor": 350.00,
                  "dataVencimento": "2026-08-10",
                  "fornecedorId": "11111111-1111-1111-1111-111111111111",
                  "situacao": "PAGA"
                }""";

        mockMvc.perform(post("/contas").contentType("application/json").content(corpoComSituacaoInvalida))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("situacao")));
    }

    @Test
    void deveRetornar400QuandoCorpoJsonMalformado() throws Exception {
        mockMvc.perform(post("/contas").contentType("application/json").content("{ isso nao e json valido"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

}
