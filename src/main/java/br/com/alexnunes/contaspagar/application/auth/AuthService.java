package br.com.alexnunes.contaspagar.application.auth;

import br.com.alexnunes.contaspagar.infrastructure.security.AdminSecurityProperties;
import br.com.alexnunes.contaspagar.infrastructure.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AdminSecurityProperties adminSecurityProperties;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AdminSecurityProperties adminSecurityProperties, JwtService jwtService,
                        PasswordEncoder passwordEncoder) {
        this.adminSecurityProperties = adminSecurityProperties;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public String autenticar(String usuario, String senha) {
        if (!adminSecurityProperties.adminUser().equals(usuario)
                || !passwordEncoder.matches(senha, adminSecurityProperties.adminPasswordHash())) {
            throw new CredenciaisInvalidasException();
        }

        return jwtService.gerarToken(usuario);
    }

}
