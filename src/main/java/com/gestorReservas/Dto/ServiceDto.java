package com.gestorReservas.Dto;

import com.gestorReservas.Model.Service;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDto {

    private Long id;
    private String name;
    private BigDecimal price;
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
