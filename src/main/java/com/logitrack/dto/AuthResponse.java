package com.logitrack.dto;

import com.logitrack.enums.Rol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;

    private String tipo;

    private String username;

    private Rol rol;
}