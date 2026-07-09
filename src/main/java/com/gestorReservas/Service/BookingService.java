package com.gestorReservas.Service;

import com.gestorReservas.Dto.BookingDto;
import com.gestorReservas.Model.Booking;
import com.gestorReservas.Model.Business;
import com.gestorReservas.Model.Employee;
import com.gestorReservas.Model.Service;
import com.gestorReservas.Model.User;
import com.gestorReservas.Repository.BookingRepository;
import com.gestorReservas.Repository.EmployeeRepository;
import com.gestorReservas.Repository.ServiceRepository;
import com.gestorReservas.Repository.UserRepository;
import com.gestorReservas.exception.ApiException;
import org.springframework.http.HttpStatus;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@org.springframework.stereotype.Service
public class BookingService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final ServiceRepository serviceRepository;
    private final EmployeeRepository employeeRepository;

    public BookingService(
            UserRepository userRepository,
            BookingRepository bookingRepository,
            ServiceRepository serviceRepository,
            EmployeeRepository employeeRepository
    ) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.serviceRepository = serviceRepository;
        this.employeeRepository = employeeRepository;
    }

    public String createBooking(
            Principal principal,
            Long serviceId,
            Long employeeId,
            LocalDateTime startAt,
            String customerName,
            String customerPhone
    ) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "no autenticado"));

        Business business = user.getBusiness();
        if (business == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "no tienes un negocio");
        }

        if (serviceId == null || startAt == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "serviceId y startAt son obligatorios");
        }

        if (customerName == null || customerName.trim().isEmpty()
                || customerPhone == null || customerPhone.trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "customerName y customerPhone son obligatorios");
        }

        if (startAt.isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "startAt no puede ser en el pasado");
        }

        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "servicio no encontrado"));

        if (!service.getBusiness().getBusinessId().equals(business.getBusinessId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "no tienes permiso");
        }

        Employee employee = null;
        if (employeeId != null) {
            employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "empleado no encontrado"));

            if (!employee.getBusiness().getBusinessId().equals(business.getBusinessId())) {
                throw new ApiException(HttpStatus.FORBIDDEN, "no tienes permiso");
            }

            if (!employee.isActive()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "el empleado no está activo");
            }

            LocalDateTime newEndAt = startAt.plusMinutes(service.getDuration());
            if (hasOverlap(employee.getId(), startAt, newEndAt, null)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "el empleado ya tiene una reserva en ese horario");
            }
        }

        Booking booking = new Booking();
        booking.setBusiness(business);
        booking.setService(service);
        booking.setEmployee(employee);
        booking.setStartAt(startAt);
        booking.setCustomerName(customerName.trim());
        booking.setCustomerPhone(customerPhone.trim()); 
        booking.setCreated_at(LocalDateTime.now());
        bookingRepository.save(booking);

        return "reserva creada";
    }

    private boolean hasOverlap(Long employeeId, LocalDateTime requestedStartAt, LocalDateTime requestedEndAt, Long currentBookingId) {
        List<Booking> employeeBookings = bookingRepository.findByEmployeeId(employeeId);

        for (Booking employeeBooking : employeeBookings) {
            if (currentBookingId != null && currentBookingId.equals(employeeBooking.getId())) {
                continue;
            }

            LocalDateTime bookingStartAt = employeeBooking.getStartAt();
            LocalDateTime bookingEndAt = bookingStartAt.plusMinutes(employeeBooking.getService().getDuration());

            if (requestedStartAt.isBefore(bookingEndAt) && requestedEndAt.isAfter(bookingStartAt)) {
                return true;
            }
        }

        return false;
    }

    public List<BookingDto> getAll(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "no autenticado"));

        Business business = user.getBusiness();
        if (business == null) {
            return Collections.emptyList();
        }

        List<Booking> bookings = bookingRepository.findByBusinessBusinessId(business.getBusinessId());
        List<BookingDto> resultado = new ArrayList<>();
        for (Booking booking : bookings) {
            resultado.add(BookingDto.from(booking));
        }
        return resultado;
    }


    public String editBooking(Principal principal, Long id, Long serviceId, Long employeeId,
            LocalDateTime startAt, String customerName, String customerPhone){


        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "no autenticado"));

        Business business = user.getBusiness();
        if (business == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "no tienes un negocio");
        }

        if (serviceId == null || startAt == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "serviceId y startAt son obligatorios");
        }

        if (customerName == null || customerName.trim().isEmpty()
                || customerPhone == null || customerPhone.trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "customerName y customerPhone son obligatorios");
        }

        if (startAt.isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "startAt no puede ser en el pasado");
        }

        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "servicio no encontrado"));

        if (!service.getBusiness().getBusinessId().equals(business.getBusinessId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "no tienes permiso");
        }


        Employee employee = null;
        if (employeeId != null) {
            employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "empleado no encontrado"));

            if (!employee.getBusiness().getBusinessId().equals(business.getBusinessId())) {
                throw new ApiException(HttpStatus.FORBIDDEN, "no tienes permiso");
            }

            if (!employee.isActive()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "el empleado no está activo");
            }

            LocalDateTime newEndAt = startAt.plusMinutes(service.getDuration());
            if (hasOverlap(employee.getId(), startAt, newEndAt, id)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "el empleado ya tiene una reserva en ese horario");
            }
        }

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "reserva no encontrada"));

        if (!booking.getBusiness().getBusinessId().equals(business.getBusinessId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "no tienes permiso");
        }

        booking.setEmployee(employee);
        booking.setService(service);
        booking.setCustomerName(customerName.trim());
        booking.setCustomerPhone(customerPhone.trim());
        booking.setStartAt(startAt);
        bookingRepository.save(booking);

        return "reserva modificada";
    }


    public String deleteBooking(Principal principal, Long id) {

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "no autenticado"));

        Business business = user.getBusiness();
        if (business == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "no tienes un negocio");
        }

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "reserva no encontrada"));

        if(!user.getBusiness().getBusinessId().equals(booking.getBusiness().getBusinessId())){
            throw new ApiException(HttpStatus.FORBIDDEN, "no tienes permiso");
        }

        bookingRepository.delete(booking);

        return "reserva eliminada";
    }
}

