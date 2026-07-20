package com.gestorReservas.Service;

import com.gestorReservas.Model.Booking;
import com.gestorReservas.Model.Business;
import com.gestorReservas.Model.Service;
import com.gestorReservas.Model.User;
import com.gestorReservas.Repository.BookingRepository;
import com.gestorReservas.Repository.BusinessRepository;
import com.gestorReservas.Repository.BusinessScheduleRepository;
import com.gestorReservas.Repository.EmployeeRepository;
import com.gestorReservas.Repository.EmployeeScheduleRepository;
import com.gestorReservas.Repository.ServiceRepository;
import com.gestorReservas.Repository.UserRepository;
import com.gestorReservas.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ServiceRepository serviceRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private BusinessRepository businessRepository;
    @Mock
    private BusinessScheduleRepository businessScheduleRepository;
    @Mock
    private EmployeeScheduleRepository employeeScheduleRepository;

    @InjectMocks
    private BookingService bookingService;

    private Principal principal(String email) {
        return () -> email;
    }

    @Test
    void createBooking_sinNegocio_lanzaBadRequest() {
        User user = new User();
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(user));

        ApiException ex = assertThrows(ApiException.class, () ->
                bookingService.createBooking(
                        principal("owner@test.com"),
                        1L,
                        null,
                        LocalDateTime.now().plusDays(1),
                        "Cliente",
                        "600000000"
                ));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("no tienes un negocio", ex.getMessage());
    }

    @Test
    void createBooking_servicioDeOtroNegocio_lanzaForbidden() {
        Business myBusiness = new Business();
        myBusiness.setBusinessId(1L);
        User user = new User();
        user.setBusiness(myBusiness);

        Business otherBusiness = new Business();
        otherBusiness.setBusinessId(2L);
        Service service = new Service();
        service.setBusiness(otherBusiness);

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(user));
        when(serviceRepository.findById(99L)).thenReturn(Optional.of(service));

        ApiException ex = assertThrows(ApiException.class, () ->
                bookingService.createBooking(
                        principal("owner@test.com"),
                        99L,
                        null,
                        LocalDateTime.now().plusDays(1),
                        "Cliente",
                        "600000000"
                ));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("no tienes permiso", ex.getMessage());
    }

    @Test
    void deleteBooking_deOtroNegocio_lanzaForbidden() {
        Business myBusiness = new Business();
        myBusiness.setBusinessId(1L);
        User user = new User();
        user.setBusiness(myBusiness);

        Business otherBusiness = new Business();
        otherBusiness.setBusinessId(2L);
        Booking booking = new Booking();
        booking.setBusiness(otherBusiness);

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(user));
        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));

        ApiException ex = assertThrows(ApiException.class, () ->
                bookingService.deleteBooking(principal("owner@test.com"), 5L));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("no tienes permiso", ex.getMessage());
        verify(bookingRepository, never()).delete(any());
    }

    @Test
    void deleteBooking_propia_elimina() {
        Business business = new Business();
        business.setBusinessId(1L);
        User user = new User();
        user.setBusiness(business);

        Booking booking = new Booking();
        booking.setBusiness(business);

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(user));
        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));

        String result = bookingService.deleteBooking(principal("owner@test.com"), 5L);

        assertEquals("reserva eliminada", result);
        verify(bookingRepository).delete(booking);
    }

    @Test
    void editBooking_deOtroNegocio_lanzaForbidden() {
        Business myBusiness = new Business();
        myBusiness.setBusinessId(1L);
        User user = new User();
        user.setBusiness(myBusiness);

        Service service = new Service();
        service.setBusiness(myBusiness);
        service.setDuration(30);

        Business otherBusiness = new Business();
        otherBusiness.setBusinessId(2L);
        Booking booking = new Booking();
        booking.setBusiness(otherBusiness);

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(user));
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
        when(businessScheduleRepository.findByBusiness_BusinessIdAndDayOfWeek(eq(1L), any(DayOfWeek.class)))
                .thenReturn(java.util.List.of(openAllDaySchedule(myBusiness)));
        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));

        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);

        ApiException ex = assertThrows(ApiException.class, () ->
                bookingService.editBooking(
                        principal("owner@test.com"),
                        5L,
                        1L,
                        null,
                        start,
                        "Cliente",
                        "600000000"
                ));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("no tienes permiso", ex.getMessage());
    }

    @Test
    void createPublicBooking_telefonoYaReservadoEseDia_lanzaBadRequest() {
        Business business = new Business();
        business.setBusinessId(1L);
        when(businessRepository.findBySlug("mi-negocio")).thenReturn(Optional.of(business));
        when(bookingRepository.existsByBusinessAndPhoneOnDay(eq(1L), eq("600000000"), any(), any()))
                .thenReturn(true);

        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);

        ApiException ex = assertThrows(ApiException.class, () ->
                bookingService.createPublicBooking(
                        "mi-negocio",
                        1L,
                        null,
                        start,
                        "Cliente",
                        "600 000 000"
                ));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("Ya tienes una reserva este dia con este numero", ex.getMessage());
    }

    @Test
    void createPublicBooking_servicioDeOtroNegocio_lanzaForbidden() {
        Business business = new Business();
        business.setBusinessId(1L);
        Business other = new Business();
        other.setBusinessId(2L);
        Service service = new Service();
        service.setBusiness(other);

        when(businessRepository.findBySlug("mi-negocio")).thenReturn(Optional.of(business));
        when(bookingRepository.existsByBusinessAndPhoneOnDay(eq(1L), eq("600000000"), any(), any()))
                .thenReturn(false);
        when(serviceRepository.findById(99L)).thenReturn(Optional.of(service));

        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);

        ApiException ex = assertThrows(ApiException.class, () ->
                bookingService.createPublicBooking(
                        "mi-negocio",
                        99L,
                        null,
                        start,
                        "Cliente",
                        "600000000"
                ));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("no tienes permiso", ex.getMessage());
    }

    private com.gestorReservas.Model.BusinessSchedule openAllDaySchedule(Business business) {
        com.gestorReservas.Model.BusinessSchedule schedule = new com.gestorReservas.Model.BusinessSchedule();
        schedule.setBusiness(business);
        schedule.setOpenTime(java.time.LocalTime.of(0, 0));
        schedule.setCloseTime(java.time.LocalTime.of(23, 59));
        return schedule;
    }
}
