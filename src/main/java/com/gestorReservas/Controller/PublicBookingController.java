package com.gestorReservas.Controller;

import com.gestorReservas.Dto.BookingRequest;
import com.gestorReservas.Service.BookingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequestMapping("/api/public/{slug}/bookings")
public class PublicBookingController {

    private final BookingService bookingService;
    public PublicBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }
    @PostMapping
    public ResponseEntity<String> createPublicBooking(
            @PathVariable String slug,
            @RequestBody BookingRequest req
    ) {
        String status = bookingService.createPublicBooking(
                slug,
                req.getServiceId(),
                req.getEmployeeId(),
                req.getStartAt(),
                req.getCustomerName(),
                req.getCustomerPhone()
        );
        return new ResponseEntity<>(status, HttpStatus.CREATED);
    }

    @GetMapping("/availability")
    public ResponseEntity<List<LocalDateTime>> getAvailability(
            @PathVariable String slug,
            @RequestParam Long serviceId,
            @RequestParam Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<LocalDateTime> slots = bookingService.getAvailability(slug, serviceId, employeeId, date);
        return ResponseEntity.ok(slots);
    }
}
