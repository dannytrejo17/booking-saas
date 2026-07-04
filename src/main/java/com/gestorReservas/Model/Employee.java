package com.gestorReservas.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    private Long id;
    private Business business;
    private String name;
    private boolean active;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

}
