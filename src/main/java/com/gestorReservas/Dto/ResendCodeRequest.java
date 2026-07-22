package com.gestorReservas.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResendCodeRequest {

    @NotBlank(message = "debe ingresar el email")
    @Email(message = "email no valido")
    private String email;
}
