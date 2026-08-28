package com.logitrack.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.logitrack.dto.AuthResponse;
import com.logitrack.dto.LoginRequest;
import com.logitrack.dto.RegisterRequest;
import com.logitrack.services.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registrar(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse respuesta = authService.registrar(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(respuesta);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> iniciarSesion(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse respuesta = authService.iniciarSesion(request);

        return ResponseEntity.ok(respuesta);
    }
}