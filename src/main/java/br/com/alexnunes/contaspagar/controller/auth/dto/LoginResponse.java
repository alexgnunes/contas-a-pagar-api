package br.com.alexnunes.contaspagar.controller.auth.dto;

public record LoginResponse(String token, String tipo) {

    public LoginResponse(String token) {
        this(token, "Bearer");
    }

}
