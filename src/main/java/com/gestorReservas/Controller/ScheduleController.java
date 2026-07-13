package com.gestorReservas.Controller;


import com.gestorReservas.Dto.ScheduleDto;
import com.gestorReservas.Dto.ScheduleRequest;
import com.gestorReservas.Service.BusinessService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {


    private final BusinessService businessService;

    public ScheduleController(BusinessService businessService) {
        this.businessService = businessService;
    }


    @PostMapping()
    public ResponseEntity<String> createSchedule(@RequestBody ScheduleRequest scheduleRequest,
                                                 Principal principal){
        String status = businessService.createSchedule(principal, scheduleRequest.getOpenTime(),
                scheduleRequest.getCloseTime(), scheduleRequest.getDayOfWeek());

        return new ResponseEntity<>(status, HttpStatus.CREATED);
    }

    @GetMapping()
    public ResponseEntity<List<ScheduleDto>> getSchedule(Principal principal) {
        List<ScheduleDto> schedules = businessService.getSchedule(principal);
        return ResponseEntity.ok(schedules);
    }
}
