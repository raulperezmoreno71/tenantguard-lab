package dev.tenantguard.tenantguard_lab.controller;

import dev.tenantguard.tenantguard_lab.dto.LoginRequest;
import dev.tenantguard.tenantguard_lab.dto.LoginResponse;
import dev.tenantguard.tenantguard_lab.dto.RegisterRequest;
import dev.tenantguard.tenantguard_lab.dto.RegisterResponse;
import dev.tenantguard.tenantguard_lab.entity.User;
import dev.tenantguard.tenantguard_lab.service.AuthService;
import dev.tenantguard.tenantguard_lab.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;

    public AuthController(AuthService authService, TokenService tokenService) {
        this.authService = authService;
        this.tokenService = tokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request.username(), request.password());

        RegisterResponse response = new RegisterResponse(user.getId(), user.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = authService.authenticate(request.username(), request.password());

        String token = tokenService.generateToken(user);

        LoginResponse response = new LoginResponse(token);

        return ResponseEntity.ok(response);
    }
}
