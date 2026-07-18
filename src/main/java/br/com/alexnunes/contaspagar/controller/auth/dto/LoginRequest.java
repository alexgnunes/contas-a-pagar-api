package br.com.alexnunes.contaspagar.controller.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String usuario,
        @NotBlank String senha) {
}
