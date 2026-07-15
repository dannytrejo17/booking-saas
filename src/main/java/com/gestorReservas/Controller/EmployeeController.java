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
import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    public ResponseEntity<Map<String,String>> createEmployee(@RequestBody EmployeeDto req, Principal principal) {
        String status = employeeService.createEmployee(principal, req.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", status));
    }

    @GetMapping
    public ResponseEntity<List<EmployeeDto>> getEmployees(Principal principal) {
        List<EmployeeDto> employees = employeeService.getAll(principal);
        return ResponseEntity.ok(employees);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String,String>> editEmployee(@PathVariable Long id, @RequestBody EmployeeDto req, Principal principal) {
        String status = employeeService.editEmployee(principal, id, req.getName(), req.isActive());
        return ResponseEntity.ok(Map.of("message", status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String,String>> deleteEmployee(@PathVariable Long id, Principal principal) {
        String status = employeeService.deleteEmployee(principal, id);
        return ResponseEntity.ok(Map.of("message", status));
    }

    @PostMapping("/{id}/schedule")
    public ResponseEntity<Map<String,String>> createEmployeeSchedule(@PathVariable Long id,
                                                         @RequestBody ScheduleRequest scheduleRequest,
                                                         Principal principal){
        String status = employeeService.createEmployeeSchedule(principal, id,
                scheduleRequest.getDayOfWeek(), scheduleRequest.getOpenTime(), scheduleRequest.getCloseTime());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", status));
    }

    @GetMapping("/{id}/schedule")
    public ResponseEntity<List<ScheduleDto>> getEmployeeSchedule(@PathVariable Long id, Principal principal){

        List<ScheduleDto> employeeSchedule = employeeService.getEmployeeSchedule(principal, id);
        return  ResponseEntity.ok(employeeSchedule);
    }

    @DeleteMapping("/{id}/schedule")
    public ResponseEntity<Map<String,String>> deleteEmployeeSchedule(@PathVariable Long id,
                                                         @RequestParam DayOfWeek dayOfWeek,
                                                         Principal principal){
        String status = employeeService.deleteEmployeeSchedule(principal,id, dayOfWeek);
        return ResponseEntity.ok(Map.of("message", status));
    }
}
