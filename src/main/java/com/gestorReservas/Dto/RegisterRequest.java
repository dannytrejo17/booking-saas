package com.gestorReservas.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "nombre es requerido")
    private String name;
    @NotBlank(message = "email es requerido")
    @Email(message = "email no valido")
    private String email;

    @NotBlank(message = "contraseña es requerida")
    @Size(min = 8, message = "contraseña debe tener al menos 8 caracteres")
    private String password;

}
