package com.gestorReservas.Service;

import com.gestorReservas.Dto.EmployeeDto;
import com.gestorReservas.Dto.ScheduleDto;
import com.gestorReservas.Model.Business;
import com.gestorReservas.Model.Employee;
import com.gestorReservas.Model.EmployeeSchedule;
import com.gestorReservas.Model.User;
import com.gestorReservas.Repository.BusinessRepository;
import com.gestorReservas.Repository.EmployeeRepository;
import com.gestorReservas.Repository.EmployeeScheduleRepository;
import com.gestorReservas.Repository.UserRepository;
import com.gestorReservas.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class EmployeeService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final BusinessRepository businessRepository;
    private final EmployeeScheduleRepository employeeScheduleRepository;

    public EmployeeService(UserRepository userRepository, EmployeeRepository employeeRepository, BusinessRepository businessRepository, EmployeeScheduleRepository employeeScheduleRepository) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.businessRepository = businessRepository;
        this.employeeScheduleRepository = employeeScheduleRepository;
    }

    public String createEmployee(Principal principal, String name) {

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "no autenticado"));

        if (user.getBusiness() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "no tienes un negocio");
        }

        if (name == null || name.trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "el nombre es obligatorio");
        }

        Employee employee = new Employee();
        employee.setBusiness(user.getBusiness());
        employee.setName(name.trim());
        employee.setActive(true);
        employee.setCreated_at(LocalDateTime.now());
        employeeRepository.save(employee);

        return "empleado creado";
    }

    public List<EmployeeDto> getAll(Principal principal) {

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "no autenticado"));

        Business business = user.getBusiness();
        if (business == null) {
            return Collections.emptyList();
        }

        List<Employee> employees = employeeRepository.findByBusinessBusinessId(business.getBusinessId());
        List<EmployeeDto> resultado = new ArrayList<>();
        for (Employee employee : employees) {
            resultado.add(EmployeeDto.from(employee));
        }
        return resultado;
    }

    public String editEmployee(Principal principal, Long id, String name, boolean active) {

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "no autenticado"));

        if (user.getBusiness() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "no tienes un negocio");
        }

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "empleado no encontrado"));

        if (!employee.getBusiness().getBusinessId().equals(user.getBusiness().getBusinessId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "no tienes permiso");
        }

        if (name == null || name.trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "el nombre es obligatorio");
        }

        employee.setName(name.trim());
        employee.setActive(active);
        employee.setUpdated_at(LocalDateTime.now());
        employeeRepository.save(employee);

        return "empleado modificado";
    }

    public String deleteEmployee(Principal principal, Long id) {

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "no autenticado"));

        if (user.getBusiness() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "no tienes un negocio");
        }

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "empleado no encontrado"));

        if (!employee.getBusiness().getBusinessId().equals(user.getBusiness().getBusinessId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "no tienes permiso");
        }

        employeeRepository.delete(employee);

        return "empleado eliminado";
    }


    public List<EmployeeDto> getAllBySlug(String slug) {
        Business business = businessRepository.findBySlug(slug.trim().toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "negocio no encontrado"));

        List<Employee> employees = employeeRepository.findByBusinessBusinessId(business.getBusinessId());
        List<EmployeeDto> resultado = new ArrayList<>();
        for (Employee employee : employees) {
            if (employee.isActive()) {
                resultado.add(EmployeeDto.from(employee));
            }
        }
        return resultado;
    }

    public String createEmployeeSchedule(Principal principal, Long employeeId, DayOfWeek dayOfWeek
            ,LocalTime openTime, LocalTime closeTime){

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "no autenticado"));

        if (user.getBusiness() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "no tienes un negocio");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "empleado no encontrado"));

        if (!employee.getBusiness().getBusinessId().equals(user.getBusiness().getBusinessId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "no tienes permiso");
        }

        EmployeeSchedule employeeSchedule = new EmployeeSchedule();
        employeeSchedule.setEmployee(employee);
        employeeSchedule.setDayOfWeek(dayOfWeek);
        employeeSchedule.setOpenTime(openTime);
        employeeSchedule.setCloseTime(closeTime);
        employeeScheduleRepository.save(employeeSchedule);

        return "horario de empleado creado";
    }

    public List<ScheduleDto> getEmployeeSchedule(Principal principal, Long employeeId){

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "no autenticado"));
        if (user.getBusiness() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "no tienes un negocio");
        }
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "empleado no encontrado"));
        if (!employee.getBusiness().getBusinessId().equals(user.getBusiness().getBusinessId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "no tienes permiso");
        }

        List<EmployeeSchedule> employeeSchedules = employeeScheduleRepository.findByEmployee_Id(employeeId);
        List<ScheduleDto> employeeSchedulesResponse = new ArrayList<>();
        for(EmployeeSchedule employeeSchedule : employeeSchedules){
            employeeSchedulesResponse.add(ScheduleDto.from(employeeSchedule));
        }

        return employeeSchedulesResponse;
    }
}
