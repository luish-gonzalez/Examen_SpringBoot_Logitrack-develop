package com.logitrack.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.logitrack.dto.AuthResponse;
import com.logitrack.dto.LoginRequest;
import com.logitrack.dto.RegisterRequest;
import com.logitrack.entities.Usuario;
import com.logitrack.exceptions.BusinessException;
import com.logitrack.repositories.UsuarioRepository;
import com.logitrack.security.CustomUserDetailsService;
import com.logitrack.security.JwtService;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            CustomUserDetailsService customUserDetailsService,
            JwtService jwtService) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.customUserDetailsService = customUserDetailsService;
        this.jwtService = jwtService;
    }

    public AuthResponse registrar(RegisterRequest request) {
        String username = request.getUsername().trim();

        if (usuarioRepository.existsByUsername(username)) {
            throw new BusinessException(
                    "El nombre de usuario ya está registrado"
            );
        }

        Usuario usuario = new Usuario();

        usuario.setNombre(request.getNombre().trim());
        usuario.setUsername(username);
        usuario.setPassword(
                passwordEncoder.encode(request.getPassword())
        );
        usuario.setRol(request.getRol());
        usuario.setActivo(true);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        UserDetails userDetails =
                customUserDetailsService.loadUserByUsername(
                        usuarioGuardado.getUsername()
                );

        String token = jwtService.generarToken(userDetails);

        return construirRespuesta(usuarioGuardado, token);
    }

    public AuthResponse iniciarSesion(LoginRequest request) {
        String username = request.getUsername().trim();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        request.getPassword()
                )
        );

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(
                        "No fue posible obtener el usuario autenticado"
                ));

        UserDetails userDetails =
                customUserDetailsService.loadUserByUsername(username);

        String token = jwtService.generarToken(userDetails);

        return construirRespuesta(usuario, token);
    }

    private AuthResponse construirRespuesta(
            Usuario usuario,
            String token) {

        return new AuthResponse(
                token,
                "Bearer",
                usuario.getUsername(),
                usuario.getRol()
        );
    }
}