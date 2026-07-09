package com.gestorReservas.Service;

import com.gestorReservas.Model.Business;
import com.gestorReservas.Model.Employee;
import com.gestorReservas.Model.User;
import com.gestorReservas.Repository.EmployeeRepository;
import com.gestorReservas.Repository.UserRepository;
import com.gestorReservas.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.security.Principal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Principal principal(String email) {
        return () -> email;
    }

    @Test
    void createEmployee_sinNegocio_lanzaBadRequest() {
        User user = new User();
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(user));

        ApiException ex = assertThrows(ApiException.class, () ->
                employeeService.createEmployee(principal("owner@test.com"), "Ana"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("no tienes un negocio", ex.getMessage());
    }

    @Test
    void editEmployee_deOtroNegocio_lanzaForbidden() {
        Business myBusiness = new Business();
        myBusiness.setBusinessId(1L);
        User user = new User();
        user.setBusiness(myBusiness);

        Business otherBusiness = new Business();
        otherBusiness.setBusinessId(2L);
        Employee employee = new Employee();
        employee.setBusiness(otherBusiness);

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(user));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        ApiException ex = assertThrows(ApiException.class, () ->
                employeeService.editEmployee(principal("owner@test.com"), 1L, "Ana", true));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("no tienes permiso", ex.getMessage());
    }

    @Test
    void deleteEmployee_deOtroNegocio_lanzaForbidden() {
        Business myBusiness = new Business();
        myBusiness.setBusinessId(1L);
        User user = new User();
        user.setBusiness(myBusiness);

        Business otherBusiness = new Business();
        otherBusiness.setBusinessId(2L);
        Employee employee = new Employee();
        employee.setBusiness(otherBusiness);

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(user));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        ApiException ex = assertThrows(ApiException.class, () ->
                employeeService.deleteEmployee(principal("owner@test.com"), 1L));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("no tienes permiso", ex.getMessage());
    }
}
