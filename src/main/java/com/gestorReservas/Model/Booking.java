package com.gestorReservas.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    private Long id;
    private Business business;
    private Employee employee;
    private Service service;
    private String customerName;
    private String customerPhone;
    private LocalDate date;
    private LocalDateTime created_at;
    private LocalDateTime updatedAt;


}
