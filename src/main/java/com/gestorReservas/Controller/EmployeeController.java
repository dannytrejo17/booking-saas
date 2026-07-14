package com.gestorReservas.Controller;

import com.gestorReservas.Dto.EmployeeDto;
import com.gestorReservas.Dto.ScheduleDto;
import com.gestorReservas.Dto.ScheduleRequest;
import com.gestorReservas.Service.EmployeeService;
import org.apache.catalina.connector.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    public ResponseEntity<String> createEmployee(@RequestBody EmployeeDto req, Principal principal) {
        String status = employeeService.createEmployee(principal, req.getName());
        return new ResponseEntity<>(status, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<EmployeeDto>> getEmployees(Principal principal) {
        List<EmployeeDto> employees = employeeService.getAll(principal);
        return ResponseEntity.ok(employees);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> editEmployee(@PathVariable Long id, @RequestBody EmployeeDto req, Principal principal) {
        String status = employeeService.editEmployee(principal, id, req.getName(), req.isActive());
        return new ResponseEntity<>(status, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable Long id, Principal principal) {
        String status = employeeService.deleteEmployee(principal, id);
        return new ResponseEntity<>(status, HttpStatus.OK);
    }

    @PostMapping("/{id}/schedule")
    public ResponseEntity<String> createEmployeeSchedule(@PathVariable Long id,
                                                         @RequestBody ScheduleRequest scheduleRequest,
                                                         Principal principal){
        String status = employeeService.createEmployeeSchedule(principal, id,
                scheduleRequest.getDayOfWeek(), scheduleRequest.getOpenTime(), scheduleRequest.getCloseTime());
        return new ResponseEntity<>(status, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/schedule")
    public ResponseEntity<List<ScheduleDto>> getEmployeeSchedule(@PathVariable Long id, Principal principal){

        List<ScheduleDto> employeeSchedule = employeeService.getEmployeeSchedule(principal, id);
        return  ResponseEntity.ok(employeeSchedule);
    }
}
