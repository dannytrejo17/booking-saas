package com.gestorReservas.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "business_id", referencedColumnName = "businessId")
    private Business business;

    @ManyToOne(optional = true)
    @JoinColumn(name = "employee_id", nullable = true)
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service service;
    private String customerName;
    private String customerPhone;
    private LocalDateTime startAt;
    private LocalDateTime created_at;
    private LocalDateTime updatedAt;


}
