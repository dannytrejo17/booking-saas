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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import java.time.LocalTime;
import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


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

    @Transactional
    public String createBooking(
            Principal principal,
            Long serviceId,
            Long employeeId,
            LocalDateTime startDateTime,
            String customerName,
            String customerPhone
    ) {

        Business business = getBusinessFromPrincipal(principal);

        validateBookingInput(serviceId, startDateTime, customerName, customerPhone);


        Service service = getServiceForBusiness(serviceId, business);


        DayOfWeek dayOfWeek = startDateTime.getDayOfWeek();
        List<BusinessSchedule> businessSchedules =  verifyBusinessIsOpen(business, dayOfWeek);


        LocalTime startTime = startDateTime.toLocalTime();
        LocalTime endTime = startTime.plusMinutes(service.getDuration());
        LocalDateTime endDateTime = startDateTime.plusMinutes(service.getDuration());

        boolean isAvailable = verifyBusinessIsOpenOnRequestedHour(startTime, endTime, businessSchedules);
        if(!isAvailable){
            throw new ApiException(HttpStatus.BAD_REQUEST, "el negocio no abre ese horario");
        }

        Employee employee = null;

        if (employeeId != null) {

            employee = employeeValidation(employeeId, business, dayOfWeek);

            List<EmployeeSchedule> employeeSchedules =
                    employeeScheduleRepository.findByEmployee_IdAndDayOfWeek(employeeId, dayOfWeek);

            boolean fitsEmployeeSchedule = false;

            fitsEmployeeSchedule = verifyEmployeeScheduleAndBusinessFits(employeeSchedules, businessSchedules, startTime, endTime);

            if (!fitsEmployeeSchedule) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "el empleado no trabaja en ese horario");
            }

            LocalDateTime newEndAt = startDateTime.plusMinutes(service.getDuration());
            if (hasOverlapWithLock(employee.getId(), startDateTime, newEndAt, null)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "el empleado ya tiene una reserva en ese horario");
            }
        }else {
            employee = autoAssignEmployee(
                    business,
                    dayOfWeek,
                    businessSchedules,
                    startTime,
                    endTime,
                    startDateTime,
                    endDateTime
            );
        }

        Booking booking = new Booking();
        booking.setBusiness(business);
        booking.setService(service);
        booking.setEmployee(employee);
        booking.setStartAt(startDateTime);
        booking.setCustomerName(customerName.trim());
        booking.setCustomerPhone(customerPhone.trim()); 
        booking.setCreated_at(LocalDateTime.now());
        bookingRepository.save(booking);

        return "reserva creada";
    }

    private boolean hasOverlap(List<Booking> employeeBookings, LocalDateTime requestedStartAt,
                               LocalDateTime requestedEndAt, Long currentBookingId) {
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

    private boolean hasOverlapWithLock(Long employeeId, LocalDateTime requestedStartAt,
                                        LocalDateTime requestedEndAt, Long currentBookingId) {

        List<Booking> employeeBookings = bookingRepository.findOverlappingWithLock(
                employeeId,
                requestedEndAt,
                currentBookingId
        );

        for (Booking employeeBooking : employeeBookings) {
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

    @Transactional
    public String editBooking(Principal principal, Long id, Long serviceId, Long employeeId,
            LocalDateTime startAt, String customerName, String customerPhone){

        Business business = getBusinessFromPrincipal(principal);

        validateBookingInput(serviceId, startAt, customerName, customerPhone);

        Service service = getServiceForBusiness(serviceId, business);

        DayOfWeek dayOfWeek = startAt.getDayOfWeek();
        List<BusinessSchedule> businessSchedules = verifyBusinessIsOpen(business, dayOfWeek);

        LocalTime startTime = startAt.toLocalTime();
        LocalTime endTime = startTime.plusMinutes(service.getDuration());

        boolean isAvailable = verifyBusinessIsOpenOnRequestedHour(startTime, endTime, businessSchedules);
        if (!isAvailable) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "el negocio no abre ese horario");
        }

        Employee employee = null;
        if (employeeId != null) {

            employee = employeeValidation(employeeId, business, dayOfWeek);

            List<EmployeeSchedule> employeeSchedules =
                    employeeScheduleRepository.findByEmployee_IdAndDayOfWeek(employeeId, dayOfWeek);

            boolean fitsEmployeeSchedule =
                    verifyEmployeeScheduleAndBusinessFits(employeeSchedules, businessSchedules, startTime, endTime);

            if (!fitsEmployeeSchedule) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "el empleado no trabaja en ese horario");
            }

            LocalDateTime newEndAt = startAt.plusMinutes(service.getDuration());
            if (hasOverlapWithLock(employee.getId(), startAt, newEndAt, id)) {
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

        Business business = getBusinessFromPrincipal(principal);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "reserva no encontrada"));

        if(!business.getBusinessId().equals(booking.getBusiness().getBusinessId())){
            throw new ApiException(HttpStatus.FORBIDDEN, "no tienes permiso");
        }

        bookingRepository.delete(booking);

        return "reserva eliminada";
    }

    @Transactional
    public String createPublicBooking(String slug, Long serviceId, Long employeeId, LocalDateTime startDateTime,
                                      String customerName, String customerPhone) {

        Business business = businessRepository.findBySlug(slug.trim().toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "negocio no encontrado"));

        validateBookingInput(serviceId, startDateTime, customerName, customerPhone);

        String phone = normalizePhone(customerPhone);
        validateOneBookingPerPhonePerDay(business, phone, startDateTime);

        Service service = getServiceForBusiness(serviceId, business);


        DayOfWeek dayOfWeek = startDateTime.getDayOfWeek();
        List<BusinessSchedule> businessSchedules =  verifyBusinessIsOpen(business, dayOfWeek);


        LocalTime startTime = startDateTime.toLocalTime();
        LocalTime endTime = startTime.plusMinutes(service.getDuration());
        LocalDateTime endDateTime = startDateTime.plusMinutes(service.getDuration());

        boolean isAvailable = verifyBusinessIsOpenOnRequestedHour(startTime, endTime, businessSchedules);
        if(!isAvailable){
            throw new ApiException(HttpStatus.BAD_REQUEST, "el negocio no abre ese horario");
        }


        Employee employee = null;
        if (employeeId != null) {

            employee = employeeValidation(employeeId, business, dayOfWeek);

            List<EmployeeSchedule> employeeSchedules = employeeScheduleRepository.findByEmployee_IdAndDayOfWeek(employeeId, dayOfWeek);

            boolean fitsEmployeeSchedule = false;

            fitsEmployeeSchedule = verifyEmployeeScheduleAndBusinessFits(employeeSchedules, businessSchedules, startTime, endTime);

            if (!fitsEmployeeSchedule) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "el empleado no trabaja en ese horario");
            }


            LocalDateTime newEndAt = startDateTime.plusMinutes(service.getDuration());
            if (hasOverlapWithLock(employee.getId(), startDateTime, newEndAt, null)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "el empleado ya tiene una reserva en ese horario");
            }
        }else {
            employee = autoAssignEmployee(
                    business,
                    dayOfWeek,
                    businessSchedules,
                    startTime,
                    endTime,
                    startDateTime,
                    endDateTime
            );
        }

        Booking booking = new Booking();
        booking.setBusiness(business);
        booking.setService(service);
        booking.setEmployee(employee);
        booking.setStartAt(startDateTime);
        booking.setCustomerName(customerName.trim());
        booking.setCustomerPhone(phone);
        booking.setCreated_at(LocalDateTime.now());
        bookingRepository.save(booking);

        return "reserva creada";
    }

    public List<LocalDateTime> getAvailability(String slug, Long serviceId, Long employeeId, LocalDate date) {
        Business business = businessRepository.findBySlug(slug.trim().toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "negocio no encontrado"));

        if (serviceId == null || date == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "serviceId y date son obligatorios");
        }

        Service service = getServiceForBusiness(serviceId, business);
        int duration = service.getDuration();
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        List<BusinessSchedule> businessSchedules = businessScheduleRepository
                .findByBusiness_BusinessIdAndDayOfWeek(business.getBusinessId(), dayOfWeek);

        if (businessSchedules.isEmpty()) {
            return Collections.emptyList();
        }

        if (employeeId == null) {
            return getAvailabilityForAnyEmployee(business, dayOfWeek, date, duration, businessSchedules);
        }

        return getAvailabilityForEmployee(business, employeeId, date, duration, businessSchedules);
    }

    private List<LocalDateTime> getAvailabilityForEmployee(
            Business business,
            Long employeeId,
            LocalDate date,
            int duration,
            List<BusinessSchedule> businessSchedules
    ) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "empleado no encontrado"));

        if (!employee.getBusiness().getBusinessId().equals(business.getBusinessId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "no tienes permiso");
        }

        if (!employee.isActive()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "el empleado no está activo");
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<EmployeeSchedule> employeeSchedules =
                employeeScheduleRepository.findByEmployee_IdAndDayOfWeek(employeeId, dayOfWeek);

        if (employeeSchedules.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        List<Booking> employeeBookings =
                bookingRepository.findByEmployeeIdAndStartAtBetween(employeeId, dayStart, dayEnd);

        Set<LocalDateTime> availableSlots = new LinkedHashSet<>();
        LocalDateTime now = LocalDateTime.now();

        for (BusinessSchedule businessSchedule : businessSchedules) {
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

                LocalDateTime slotStart = date.atTime(since);
                LocalDateTime endOfWindow = date.atTime(until);

                while (!slotStart.plusMinutes(duration).isAfter(endOfWindow)) {
                    if (!slotStart.isBefore(now)) {
                        LocalDateTime slotEnd = slotStart.plusMinutes(duration);
                        if (!hasOverlap(employeeBookings, slotStart, slotEnd, null)) {
                            availableSlots.add(slotStart);
                        }
                    }
                    slotStart = slotStart.plusMinutes(30);
                }
            }
        }

        return new ArrayList<>(availableSlots);
    }

    private List<LocalDateTime> getAvailabilityForAnyEmployee(
            Business business,
            DayOfWeek dayOfWeek,
            LocalDate date,
            int duration,
            List<BusinessSchedule> businessSchedules
    ) {
        List<EmployeeSchedule> allSchedules =
                employeeScheduleRepository.findActiveSchedulesOnDay(business.getBusinessId(), dayOfWeek);

        if (allSchedules == null || allSchedules.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, List<EmployeeSchedule>> schedulesByEmployee = new HashMap<>();
        for (EmployeeSchedule schedule : allSchedules) {
            Long id = schedule.getEmployee().getId();
            schedulesByEmployee.computeIfAbsent(id, k -> new ArrayList<>()).add(schedule);
        }

        List<Long> employeeIds = new ArrayList<>(schedulesByEmployee.keySet());
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);

        List<Booking> allBookings = employeeIds.isEmpty()
                ? Collections.emptyList()
                : bookingRepository.findByEmployeeIdInAndStartAtBetween(employeeIds, dayStart, dayEnd);

        Map<Long, List<Booking>> bookingsByEmployee = new HashMap<>();
        for (Booking booking : allBookings) {
            Long id = booking.getEmployee().getId();
            bookingsByEmployee.computeIfAbsent(id, k -> new ArrayList<>()).add(booking);
        }

        Set<LocalDateTime> availableSlots = new LinkedHashSet<>();
        LocalDateTime now = LocalDateTime.now();

        for (BusinessSchedule businessSchedule : businessSchedules) {
            LocalDateTime slotStart = date.atTime(businessSchedule.getOpenTime());
            LocalDateTime businessCloseDateTime = date.atTime(businessSchedule.getCloseTime());

            while (!slotStart.plusMinutes(duration).isAfter(businessCloseDateTime)) {
                if (!slotStart.isBefore(now)) {
                    LocalDateTime slotEnd = slotStart.plusMinutes(duration);
                    LocalTime slotStartTime = slotStart.toLocalTime();
                    LocalTime slotEndTime = slotEnd.toLocalTime();

                    for (Long employeeId : schedulesByEmployee.keySet()) {
                        List<EmployeeSchedule> employeeDaySchedules = schedulesByEmployee.get(employeeId);

                        boolean employeeWorksThisSlot = verifyEmployeeScheduleAndBusinessFits(
                                employeeDaySchedules, businessSchedules, slotStartTime, slotEndTime);

                        List<Booking> employeeBookings =
                                bookingsByEmployee.getOrDefault(employeeId, Collections.emptyList());
                        boolean employeeIsFree = !hasOverlap(employeeBookings, slotStart, slotEnd, null);

                        if (employeeWorksThisSlot && employeeIsFree) {
                            availableSlots.add(slotStart);
                            break;
                        }
                    }
                }
                slotStart = slotStart.plusMinutes(30);
            }
        }

        return new ArrayList<>(availableSlots);
    }


    private Business getBusinessFromPrincipal(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "no autenticado"));

        Business business = user.getBusiness();
        if (business == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "no tienes un negocio");
        }

        return business;
    }

    private void validateBookingInput(Long serviceId, LocalDateTime startAt,
                                      String customerName, String customerPhone) {

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
    }

    private Service getServiceForBusiness(Long serviceId, Business business){

        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "servicio no encontrado"));

        if (!service.getBusiness().getBusinessId().equals(business.getBusinessId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "no tienes permiso");
        }
        return service;
    }

    private List<BusinessSchedule> verifyBusinessIsOpen(Business business, DayOfWeek dayOfWeek){
        List<BusinessSchedule> businessSchedules = businessScheduleRepository.findByBusiness_BusinessIdAndDayOfWeek(business.getBusinessId(), dayOfWeek);
        if(businessSchedules.isEmpty()){
            throw new ApiException(HttpStatus.BAD_REQUEST, "el negocio no abre ese día");
        }
        return  businessSchedules;
    }

    private boolean verifyBusinessIsOpenOnRequestedHour(LocalTime startTime,
                                                        LocalTime endTime,
                                                        List<BusinessSchedule> businessSchedules){
        boolean isAvailable = false;
        for (BusinessSchedule schedule : businessSchedules) {
            LocalTime openTime = schedule.getOpenTime();
            LocalTime closeTime = schedule.getCloseTime();

            if (!startTime.isBefore(openTime) && !endTime.isAfter(closeTime)) {
                isAvailable = true;
                break;
            }
        }
        return isAvailable;
    }

    private Employee employeeValidation(Long employeeId, Business business, DayOfWeek dayOfWeek){

        Employee employee = employeeRepository.findById(employeeId)
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

        return employee;
    }

    private boolean verifyEmployeeScheduleAndBusinessFits(List<EmployeeSchedule> employeeSchedules,
                                                          List<BusinessSchedule> businessSchedules,
                                                          LocalTime startTime,
                                                          LocalTime endTime){

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
        }
        return  fitsEmployeeSchedule;
    }

    private Employee autoAssignEmployee(
            Business business,
            DayOfWeek dayOfWeek,
            List<BusinessSchedule> businessSchedules,
            LocalTime startTime,
            LocalTime endTime,
            LocalDateTime startDateAt,
            LocalDateTime endDateAt
    ) {
        List<EmployeeSchedule> employeeScheduleList =
                employeeScheduleRepository.findActiveSchedulesOnDay(business.getBusinessId(), dayOfWeek);

        if (employeeScheduleList == null || employeeScheduleList.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "no hay empleado disponible en ese horario");
        }

        Map<Long, List<EmployeeSchedule>> schedulesByEmployeeId = new HashMap<>();
        for (EmployeeSchedule employeeSchedule : employeeScheduleList) {
            Long employeeId = employeeSchedule.getEmployee().getId();
            schedulesByEmployeeId
                    .computeIfAbsent(employeeId, a -> new ArrayList<>())
                    .add(employeeSchedule);
        }

        for (List<EmployeeSchedule> franjas : schedulesByEmployeeId.values()) {
            Employee candidate = franjas.get(0).getEmployee();

            boolean fits = verifyEmployeeScheduleAndBusinessFits(
                    franjas, businessSchedules, startTime, endTime);

            if (!fits) {
                continue;
            }

            if (hasOverlapWithLock(candidate.getId(), startDateAt, endDateAt, null)) {
                continue;
            }
            return candidate; 
        }
        throw new ApiException(HttpStatus.BAD_REQUEST, "no hay empleado disponible en ese horario");
    }

    private String normalizePhone(String phone) {
        return phone.trim().replaceAll("[\\s\\-()]", "");
    }

    private void validateOneBookingPerPhonePerDay(Business business, String customerPhone, LocalDateTime startDateTime) {
        String phone = normalizePhone(customerPhone);
        LocalDate day = startDateTime.toLocalDate();
        LocalDateTime dayStart = day.atStartOfDay();
        LocalDateTime dayEnd = day.plusDays(1).atStartOfDay();

        boolean alreadyBooked = bookingRepository.existsByBusinessAndPhoneOnDay(
                business.getBusinessId(),
                phone,
                dayStart,
                dayEnd
        );

        if (alreadyBooked) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Ya tienes una reserva este dia con este numero");
        }
    }

}