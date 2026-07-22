package com.gestorReservas.Dto;

import com.gestorReservas.Model.Service;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDto {

    private Long id;
    @NotBlank(message = "el nombre del servicio es requerido")
    private String name;
    @NotNull(message = "el precio es obligatorio")
    @Positive(message = "el precio debe ser un numero positivo")
    private BigDecimal price;
    @Positive(message = "la duración debe ser mayor que 0")
    private int duration;

    public static ServiceDto from(Service service) {
        return new ServiceDto(
                service.getId(),
                service.getName(),
                service.getPrice(),
                service.getDuration()
        );
    }
}
