package br.com.alexnunes.contaspagar.infrastructure.importacao;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class ImportacaoRabbitConfig {

    private final RabbitMqProperties rabbitMqProperties;

    ImportacaoRabbitConfig(RabbitMqProperties rabbitMqProperties) {
        this.rabbitMqProperties = rabbitMqProperties;
    }

    @Bean
    DirectExchange importacaoExchange() {
        return new DirectExchange(rabbitMqProperties.exchange());
    }

    @Bean
    Queue importacaoQueue() {
        return new Queue(rabbitMqProperties.queue(), true);
    }

    @Bean
    Binding importacaoBinding() {
        return BindingBuilder.bind(importacaoQueue())
                .to(importacaoExchange())
                .with(rabbitMqProperties.routingKey());
    }

}
