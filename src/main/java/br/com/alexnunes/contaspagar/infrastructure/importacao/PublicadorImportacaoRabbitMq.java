package br.com.alexnunes.contaspagar.infrastructure.importacao;

import br.com.alexnunes.contaspagar.domain.importacao.PublicadorImportacao;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
class PublicadorImportacaoRabbitMq implements PublicadorImportacao {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMqProperties rabbitMqProperties;

    PublicadorImportacaoRabbitMq(RabbitTemplate rabbitTemplate, RabbitMqProperties rabbitMqProperties) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitMqProperties = rabbitMqProperties;
    }

    @Override
    public void publicar(String protocolo) {
        rabbitTemplate.convertAndSend(rabbitMqProperties.exchange(), rabbitMqProperties.routingKey(), protocolo);
    }

}
