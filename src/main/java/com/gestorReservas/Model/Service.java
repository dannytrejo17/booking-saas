package com.gestorReservas.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Service {

    private Long id;
    private Business business;
    private String name;
    private BigDecimal price;
    private int duration;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

}
