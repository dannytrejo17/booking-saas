package com.gestorReservas.Controller;


import com.gestorReservas.Dto.ServiceDto;
import com.gestorReservas.Model.Service;
import com.gestorReservas.Repository.ServiceRepository;
import com.gestorReservas.Service.BusinessServiceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final BusinessServiceService businessServiceService;

    public ServiceController(BusinessServiceService businessServiceService) {
        this.businessServiceService = businessServiceService;
    }

    @PostMapping
    public ResponseEntity<String> createService(@Valid  @RequestBody ServiceDto req, Principal principal){
        String status = businessServiceService.createService(principal, req.getName(),
                req.getPrice(), req.getDuration());

        return new ResponseEntity<>(status, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ServiceDto>> getServices(Principal principal) {
        List<ServiceDto> services = businessServiceService.getAll(principal);
        return ResponseEntity.ok(services);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> EditServices(@PathVariable Long id, @RequestBody ServiceDto serviceDto, Principal principal){

        String status = businessServiceService.editProduct(principal, serviceDto.getName(),
                serviceDto.getPrice(), serviceDto.getDuration(), id );

        return new  ResponseEntity<>(status, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> DeleteService(@PathVariable Long id, Principal principal){

        String status = businessServiceService.deleteservice(principal, id);
        return new ResponseEntity<>(status, HttpStatus.OK);
    }
}
