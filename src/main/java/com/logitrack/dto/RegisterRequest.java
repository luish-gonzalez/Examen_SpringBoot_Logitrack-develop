package com.logitrack.dto;

import com.logitrack.enums.Rol;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(
            max = 100,
            message = "El nombre no puede superar los 100 caracteres"
    )
    private String nombre;

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
            max = 100,
            message = "La contraseña debe tener entre 8 y 100 caracteres"
    )
    @ToString.Exclude
    private String password;

    @NotNull(message = "El rol es obligatorio")
    private Rol rol;
}