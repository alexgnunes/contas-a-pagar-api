package br.com.alexnunes.contaspagar.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record AdminSecurityProperties(String adminUser, String adminPasswordHash) {
}
