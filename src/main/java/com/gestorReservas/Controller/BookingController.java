package com.gestorReservas.Controller;

import com.gestorReservas.Dto.BookingRequest;
import com.gestorReservas.Service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<String> createBooking(@RequestBody BookingRequest req, Principal principal) {
        String status = bookingService.createBooking(
                principal,
                req.getServiceId(),
                req.getEmployeeId(),
                req.getStartAt(),
                req.getCustomerName(),
                req.getCustomerPhone()
        );
        return new ResponseEntity<>(status, HttpStatus.CREATED);
    }
}
