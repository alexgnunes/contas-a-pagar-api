package br.com.alexnunes.contaspagar.application.auth;

public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException() {
        super("Usuário ou senha inválidos");
    }

}
