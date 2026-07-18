package br.com.alexnunes.contaspagar.controller.auth;

import br.com.alexnunes.contaspagar.application.auth.AuthService;
import br.com.alexnunes.contaspagar.controller.auth.dto.LoginRequest;
import br.com.alexnunes.contaspagar.controller.auth.dto.LoginResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @SecurityRequirements
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.autenticar(request.usuario(), request.senha());
        return ResponseEntity.ok(new LoginResponse(token));
    }

}
