package com.gestorReservas.Service;

import com.gestorReservas.Dto.EmployeeDto;
import com.gestorReservas.Model.Business;
import com.gestorReservas.Model.Employee;
import com.gestorReservas.Model.User;
import com.gestorReservas.Repository.EmployeeRepository;
import com.gestorReservas.Repository.UserRepository;
import com.gestorReservas.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class EmployeeService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    public EmployeeService(UserRepository userRepository, EmployeeRepository employeeRepository) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
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
}
