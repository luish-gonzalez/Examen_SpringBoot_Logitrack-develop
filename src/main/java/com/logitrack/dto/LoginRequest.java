package com.logitrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(
            min = 4,
            max = 50,
            message = "El nombre de usuario debe tener entre 4 y 50 caracteres"
    )
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(
            min = 8,
            message = "La contraseña debe tener al menos 8 caracteres"
    )
    private String password;
}