package br.com.alexnunes.contaspagar.infrastructure.importacao;

import br.com.alexnunes.contaspagar.application.importacao.ImportacaoService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
class ImportacaoConsumer {

    private final ImportacaoService importacaoService;

    ImportacaoConsumer(ImportacaoService importacaoService) {
        this.importacaoService = importacaoService;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue}")
    public void consumir(String protocolo) {
        importacaoService.processarMensagem(protocolo);
    }

}
