package com.gestorReservas.Service;

import com.gestorReservas.Dto.BookingDto;
import com.gestorReservas.Model.Booking;
import com.gestorReservas.Model.Business;
import com.gestorReservas.Model.BusinessSchedule;
import com.gestorReservas.Model.Employee;
import com.gestorReservas.Model.EmployeeSchedule;
import com.gestorReservas.Model.Service;
import com.gestorReservas.Model.User;
import com.gestorReservas.Repository.*;
import com.gestorReservas.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import java.time.LocalTime;
import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class BookingService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final ServiceRepository serviceRepository;
    private final EmployeeRepository employeeRepository;
    private final BusinessRepository businessRepository;
    private final BusinessScheduleRepository businessScheduleRepository;
    private final EmployeeScheduleRepository employeeScheduleRepository;

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

        DayOfWeek dayOfWeek = startAt.getDayOfWeek();
        List<BusinessSchedule> schedules = businessScheduleRepository.findByBusiness_BusinessIdAndDayOfWeek(business.getBusinessId(), dayOfWeek);
        if(schedules.isEmpty()){
            throw new ApiException(HttpStatus.BAD_REQUEST, "el negocio no abre ese día");
        }

        boolean isAvailable = false;
        LocalTime startTime = startAt.toLocalTime();
        LocalTime endTime = startTime.plusMinutes(service.getDuration());

        for (BusinessSchedule schedule : schedules) {
            LocalTime openTime = schedule.getOpenTime();
            LocalTime closeTime = schedule.getCloseTime();

            if(startAt.toLocalTime().isAfter(openTime) && startAt.toLocalTime().isBefore(closeTime)
            && !endTime.isAfter(closeTime)){
                isAvailable = true;
                break;
            }
        }
        if(!isAvailable){
            throw new ApiException(HttpStatus.BAD_REQUEST, "el negocio no abre ese horario");
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

            List<EmployeeSchedule> employeeSchedules =
                    employeeScheduleRepository.findByEmployee_IdAndDayOfWeek(employeeId, dayOfWeek);

            if(employeeSchedules.isEmpty()){
                throw new ApiException(HttpStatus.BAD_REQUEST, "el empleado no trabaja ese dia");
            }

            boolean fitsEmployeeSchedule = false;

            for(BusinessSchedule businessSchedule : schedules ){
                for(EmployeeSchedule employeeSchedule : employeeSchedules){

                    LocalTime since;
                    if(businessSchedule.getOpenTime().isAfter(employeeSchedule.getOpenTime())){
                        since = businessSchedule.getOpenTime();
                    }else {
                        since = employeeSchedule.getOpenTime();
                    }

                    LocalTime until;
                    if(businessSchedule.getCloseTime().isBefore(employeeSchedule.getCloseTime())){
                        until = businessSchedule.getCloseTime();
                    }else{
                        until = employeeSchedule.getCloseTime();
                    }

                    if (!since.isBefore(until)) {
                        continue;
                    }

                    if (!startTime.isBefore(since) && !endTime.isAfter(until)) {
                        fitsEmployeeSchedule = true;
                        break;
                    }

                }

                if (fitsEmployeeSchedule) {
                    break;
                }

            }

            if (!fitsEmployeeSchedule) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "el empleado no trabaja en ese horario");
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




    public String createPublicBooking(String slug, Long serviceId, Long employeeId, LocalDateTime startAt,
                                      String customerName, String customerPhone) {

        Business business = businessRepository.findBySlug(slug.trim().toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "negocio no encontrado"));

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


        DayOfWeek dayOfWeek = startAt.getDayOfWeek();
        List<BusinessSchedule> businessSchedules = businessScheduleRepository.findByBusiness_BusinessIdAndDayOfWeek(business.getBusinessId(), dayOfWeek);
        if(businessSchedules.isEmpty()){
            throw new ApiException(HttpStatus.BAD_REQUEST, "el negocio no abre ese día");
        }
        
        boolean isAvailable = false;
        LocalTime startTime = startAt.toLocalTime();
        LocalTime endTime = startTime.plusMinutes(service.getDuration());

        for (BusinessSchedule schedule : businessSchedules) {
            LocalTime openTime = schedule.getOpenTime();
            LocalTime closeTime = schedule.getCloseTime();

            if(startAt.toLocalTime().isAfter(openTime) && startAt.toLocalTime().isBefore(closeTime) 
            && !endTime.isAfter(closeTime)){
                isAvailable = true;
                break;
            }
        }
        if(!isAvailable){
            throw new ApiException(HttpStatus.BAD_REQUEST, "el negocio no abre ese horario");
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


            List<EmployeeSchedule> employeeSchedules = employeeScheduleRepository.findByEmployee_IdAndDayOfWeek(employeeId, dayOfWeek);
            if(employeeSchedules.isEmpty()){
                throw new ApiException(HttpStatus.BAD_REQUEST, "el empleado no trabaja ese dia");
            }

            boolean fitsEmployeeSchedule = false;

            for(BusinessSchedule businessSchedule : businessSchedules){
                for (EmployeeSchedule employeeSchedule: employeeSchedules){

                    LocalTime since;
                    if(businessSchedule.getOpenTime().isAfter(employeeSchedule.getOpenTime())){
                        since = businessSchedule.getOpenTime();
                    }else{
                        since = employeeSchedule.getOpenTime();
                    }

                    LocalTime until;
                    if(businessSchedule.getCloseTime().isBefore(employeeSchedule.getCloseTime())){
                        until = businessSchedule.getCloseTime();
                    }else{
                        until = employeeSchedule.getCloseTime();
                    }

                    if(!since.isBefore(until)){
                        continue;
                    }

                    if (!startTime.isBefore(since) && !endTime.isAfter(until)) {
                        fitsEmployeeSchedule = true;
                        break;
                    }

                }
                if(fitsEmployeeSchedule){
                    break;
                }
            }

            if (!fitsEmployeeSchedule) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "el empleado no trabaja en ese horario");
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

    public List<LocalDateTime> getAvailability(String slug, Long serviceId, Long employeeId, LocalDate date) {
        Business business = businessRepository.findBySlug(slug.trim().toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "negocio no encontrado"));

        if (serviceId == null || employeeId == null || date == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "serviceId, employeeId y date son obligatorios");
        }

        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "servicio no encontrado"));

        if (!service.getBusiness().getBusinessId().equals(business.getBusinessId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "no tienes permiso");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "empleado no encontrado"));

        if (!employee.getBusiness().getBusinessId().equals(business.getBusinessId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "no tienes permiso");
        }

        if (!employee.isActive()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "el empleado no está activo");
        }



        int duration = service.getDuration();

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<BusinessSchedule> schedules = businessScheduleRepository
                .findByBusiness_BusinessIdAndDayOfWeek(business.getBusinessId(), dayOfWeek);

        if (schedules.isEmpty()) {
            return Collections.emptyList();
        }

        List<EmployeeSchedule> employeeSchedules =
                employeeScheduleRepository.findByEmployee_IdAndDayOfWeek(employeeId, dayOfWeek);

        if (employeeSchedules.isEmpty()) {
            return Collections.emptyList();
        }

        List<LocalDateTime> available = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (BusinessSchedule businessSchedule : schedules) {
            for (EmployeeSchedule employeeSchedule : employeeSchedules) {

                LocalTime since;
                if (businessSchedule.getOpenTime().isAfter(employeeSchedule.getOpenTime())) {
                    since = businessSchedule.getOpenTime();
                } else {
                    since = employeeSchedule.getOpenTime();
                }

                LocalTime until;
                if (businessSchedule.getCloseTime().isBefore(employeeSchedule.getCloseTime())) {
                    until = businessSchedule.getCloseTime();
                } else {
                    until = employeeSchedule.getCloseTime();
                }

                if (!since.isBefore(until)) {
                    continue;
                }

                LocalDateTime startCandidate = date.atTime(since);
                LocalDateTime endOfWindow = date.atTime(until);

                while (!startCandidate.plusMinutes(duration).isAfter(endOfWindow)) {
                    if (!startCandidate.isBefore(now)) {
                        LocalDateTime endOfService = startCandidate.plusMinutes(duration);
                        if (!hasOverlap(employee.getId(), startCandidate, endOfService, null)) {
                            available.add(startCandidate);
                        }
                    }
                    startCandidate = startCandidate.plusMinutes(30);
                }
            }
        }

        return available;
    }
}
