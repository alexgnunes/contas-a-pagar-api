package br.com.alexnunes.contaspagar.controller.fornecedor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FornecedorRequest(
        @NotBlank(message = "não pode estar em branco") @Size(max = 255, message = "deve ter no máximo 255 caracteres") String nome) {
}
