package com.gestorReservas.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "email es requerido")
    @Email(message = "email no valido")
    private String email;

    @NotBlank(message = "contraseña es requerida")
    private String password;
}
