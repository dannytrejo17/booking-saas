package com.gestorReservas.Controller;

import com.gestorReservas.Dto.EmployeeDto;
import com.gestorReservas.Service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/public/{slug}/employees")
public class PublicEmployeeController {

        private final EmployeeService employeeService;

        public PublicEmployeeController(EmployeeService employeeService) {
            this.employeeService = employeeService;
        }

        @GetMapping
        public ResponseEntity<List<EmployeeDto>> getEmployees(@PathVariable String slug) {
            List<EmployeeDto> employees = employeeService.getAllBySlug(slug);
            return ResponseEntity.ok(employees);
        }
    }

