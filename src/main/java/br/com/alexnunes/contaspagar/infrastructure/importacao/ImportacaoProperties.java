package br.com.alexnunes.contaspagar.infrastructure.importacao;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.importacao")
public record ImportacaoProperties(String diretorio) {
}
