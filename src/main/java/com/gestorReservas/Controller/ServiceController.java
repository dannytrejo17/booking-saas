package com.gestorReservas.Controller;


import com.gestorReservas.Dto.ServiceDto;
import com.gestorReservas.Model.Service;
import com.gestorReservas.Service.BusinessServiceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final BusinessServiceService businessServiceService;

    public ServiceController(BusinessServiceService businessServiceService) {
        this.businessServiceService = businessServiceService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createService(@Valid  @RequestBody Service req, Principal principal){
        String status = businessServiceService.createService(principal, req.getName(),
                req.getPrice(), req.getDuration());

        return new ResponseEntity<>(status, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ServiceDto>> getServices(Principal principal) {
        List<ServiceDto> services = businessServiceService.getAll(principal);
        return ResponseEntity.ok(services);
    }
}
