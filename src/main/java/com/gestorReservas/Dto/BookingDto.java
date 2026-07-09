package com.gestorReservas.Dto;

import com.gestorReservas.Model.Booking;
import com.gestorReservas.Model.Employee;
import com.gestorReservas.Model.Service;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {

    private Long id;
    private Long serviceId;
    private String serviceName;
    private Long employeeId;
    private String employeeName;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String customerName;
    private String customerPhone;

    public static BookingDto from(Booking booking) {
        Service service = booking.getService();
        Employee employee = booking.getEmployee();

        LocalDateTime endAt = booking.getStartAt().plusMinutes(service.getDuration());

        return new BookingDto(
                booking.getId(),
                service.getId(),
                service.getName(),
                employee != null ? employee.getId() : null,
                employee != null ? employee.getName() : null,
                booking.getStartAt(),
                endAt,
                booking.getCustomerName(),
                booking.getCustomerPhone()
        );
    }
}
