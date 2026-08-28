package com.logitrack.security;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final String claveSecreta;
    private final long tiempoExpiracion;

    public JwtService(
            @Value("${jwt.secret}") String claveSecreta,
            @Value("${jwt.expiration}") long tiempoExpiracion) {

        this.claveSecreta = claveSecreta;
        this.tiempoExpiracion = tiempoExpiracion;
    }

    public String generarToken(UserDetails userDetails) {
        Map<String, Object> claimsAdicionales = new HashMap<>();

        List<String> autoridades = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        claimsAdicionales.put("roles", autoridades);

        return generarToken(claimsAdicionales, userDetails);
    }

    public String extraerUsername(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    public boolean esTokenValido(
            String token,
            UserDetails userDetails) {

        String username = extraerUsername(token);

        return username.equals(userDetails.getUsername())
                && !estaTokenExpirado(token)
                && userDetails.isEnabled();
    }

    public <T> T extraerClaim(
            String token,
            Function<Claims, T> resolverClaims) {

        Claims claims = extraerTodosLosClaims(token);
        return resolverClaims.apply(claims);
    }

    private String generarToken(
            Map<String, Object> claimsAdicionales,
            UserDetails userDetails) {

        Date fechaCreacion = new Date();
        Date fechaExpiracion = new Date(
                fechaCreacion.getTime() + tiempoExpiracion
        );

        return Jwts.builder()
                .claims(claimsAdicionales)
                .subject(userDetails.getUsername())
                .issuedAt(fechaCreacion)
                .expiration(fechaExpiracion)
                .signWith(obtenerClaveFirma())
                .compact();
    }

    private boolean estaTokenExpirado(String token) {
        Date fechaExpiracion = extraerClaim(
                token,
                Claims::getExpiration
        );

        return fechaExpiracion.before(new Date());
    }

    private Claims extraerTodosLosClaims(String token) {
        return Jwts.parser()
                .verifyWith(obtenerClaveFirma())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey obtenerClaveFirma() {
        byte[] bytesClave = Decoders.BASE64.decode(claveSecreta);
        return Keys.hmacShaKeyFor(bytesClave);
    }
}