package com.gestorReservas.Controller;

import com.gestorReservas.Dto.BookingDto;
import com.gestorReservas.Dto.BookingRequest;
import com.gestorReservas.Service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

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

    @GetMapping
    public ResponseEntity<List<BookingDto>> getBookings(Principal principal) {
        List<BookingDto> bookings = bookingService.getAll(principal);
        return ResponseEntity.ok(bookings);
    }


    @PutMapping("/{id}")
    public ResponseEntity<String> editBooking(
            @PathVariable Long id,
            @RequestBody BookingRequest req,
            Principal principal
    ) {
        String status = bookingService.editBooking(
                principal,
                id,
                req.getServiceId(),
                req.getEmployeeId(),
                req.getStartAt(),
                req.getCustomerName(),
                req.getCustomerPhone()
        );
        return ResponseEntity.ok(status);
    }
}
