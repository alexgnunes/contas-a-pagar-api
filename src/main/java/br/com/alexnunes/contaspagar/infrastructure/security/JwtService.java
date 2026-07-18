package br.com.alexnunes.contaspagar.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey chave;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.chave = Keys.hmacShaKeyFor(Base64.getDecoder().decode(properties.secret()));
    }

    public String gerarToken(String subject) {
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + properties.expirationMs());

        return Jwts.builder()
                .subject(subject)
                .issuedAt(agora)
                .expiration(expiracao)
                .signWith(chave)
                .compact();
    }

    public Optional<Claims> extrairClaims(String token) {
        try {
            return Optional.of(parseClaims(token));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
