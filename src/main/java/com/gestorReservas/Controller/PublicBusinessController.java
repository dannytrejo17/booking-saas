package com.gestorReservas.Controller;

import com.gestorReservas.Dto.BusinessDto;
import com.gestorReservas.Service.BusinessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicBusinessController {

    private final BusinessService businessService;

    public PublicBusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @GetMapping("/{slug}")
    public ResponseEntity<BusinessDto> getBusiness(@PathVariable String slug) {
        return ResponseEntity.ok(businessService.getBusinessBySlug(slug));
    }
}