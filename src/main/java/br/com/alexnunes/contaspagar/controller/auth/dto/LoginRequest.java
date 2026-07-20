package br.com.alexnunes.contaspagar.controller.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "não pode estar em branco") String usuario,
        @NotBlank(message = "não pode estar em branco") String senha) {
}
