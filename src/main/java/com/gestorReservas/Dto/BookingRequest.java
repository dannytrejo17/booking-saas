package com.gestorReservas.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    private Long serviceId;
    private Long employeeId;
    private LocalDateTime startAt;
    private String customerName;
    private String customerPhone;
}
