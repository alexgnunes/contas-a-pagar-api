package br.com.alexnunes.contaspagar.application.importacao;

import br.com.alexnunes.contaspagar.application.conta.ContaService;
import br.com.alexnunes.contaspagar.domain.importacao.LinhaCsvImportacao;
import br.com.alexnunes.contaspagar.domain.importacao.LinhaImportacaoInvalida;
import br.com.alexnunes.contaspagar.domain.importacao.LinhaImportacaoValida;
import br.com.alexnunes.contaspagar.domain.importacao.ResultadoValidacaoLinha;
import br.com.alexnunes.contaspagar.domain.importacao.ValidadorLinhaCsvImportacao;
import br.com.alexnunes.contaspagar.domain.importacao.exception.LinhaImportacaoInvalidaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContaImportacaoService {

    private final ContaService contaService;
    private final ValidadorLinhaCsvImportacao validador;

    public ContaImportacaoService(ContaService contaService) {
        this.contaService = contaService;
        this.validador = new ValidadorLinhaCsvImportacao();
    }

    @Transactional
    public void processarLinha(LinhaCsvImportacao linha) {
        ResultadoValidacaoLinha resultado = validador.validar(linha);

        if (resultado instanceof LinhaImportacaoInvalida invalida) {
            throw new LinhaImportacaoInvalidaException(invalida.mensagem());
        }

        LinhaImportacaoValida valida = (LinhaImportacaoValida) resultado;
        contaService.criar(valida.descricao(), valida.valor(), valida.dataVencimento(), valida.fornecedorId(),
                valida.situacao(), valida.dataPagamento());
    }

}
