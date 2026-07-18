package com.gestorReservas.Dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class VerifyRequest {
    String code;
    String email;
}
