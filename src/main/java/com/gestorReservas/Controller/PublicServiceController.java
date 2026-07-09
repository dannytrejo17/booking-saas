package com.gestorReservas.Controller;

import com.gestorReservas.Dto.ServiceDto;
import com.gestorReservas.Service.BusinessServiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/{slug}/services")
public class PublicServiceController {

    private final BusinessServiceService businessServiceService;

    public PublicServiceController(BusinessServiceService businessServiceService) {
        this.businessServiceService = businessServiceService;
    }

    @GetMapping
    public ResponseEntity<List<ServiceDto>> getServices(@PathVariable String slug) {
        List<ServiceDto> services = businessServiceService.getAllBySlug(slug);
        return ResponseEntity.ok(services);
    }
}
