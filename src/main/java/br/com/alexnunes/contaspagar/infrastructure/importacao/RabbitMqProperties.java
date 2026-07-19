package br.com.alexnunes.contaspagar.infrastructure.importacao;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rabbitmq")
public record RabbitMqProperties(String exchange, String queue, String routingKey) {
}
