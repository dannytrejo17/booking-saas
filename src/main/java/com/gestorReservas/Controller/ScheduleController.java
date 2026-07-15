package com.gestorReservas.Controller;


import com.gestorReservas.Dto.ScheduleDto;
import com.gestorReservas.Dto.ScheduleRequest;
import com.gestorReservas.Service.BusinessService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {


    private final BusinessService businessService;

    public ScheduleController(BusinessService businessService) {
        this.businessService = businessService;
    }


    @PostMapping()
    public ResponseEntity<Map<String,String>> createSchedule(@RequestBody ScheduleRequest scheduleRequest,
                                                 Principal principal){
        String status = businessService.createSchedule(principal, scheduleRequest.getOpenTime(),
                scheduleRequest.getCloseTime(), scheduleRequest.getDayOfWeek());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", status));
    }



    @GetMapping()
    public ResponseEntity<List<ScheduleDto>> getSchedule(Principal principal) {
        List<ScheduleDto> schedules = businessService.getSchedule(principal);
        return ResponseEntity.ok(schedules);
    }


    @DeleteMapping()
    public ResponseEntity<Map<String,String>> deleteSchedule(@RequestParam  DayOfWeek dayOfWeek, Principal principal){
        String status = businessService.deleteSchedule(dayOfWeek, principal);
        return ResponseEntity.ok(Map.of("message", status));

    }
}
