package com.gestorReservas.Controller;

import com.gestorReservas.Dto.BusinessDto;
import com.gestorReservas.Model.Business;
import com.gestorReservas.Service.BusinessService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/business")
public class BusinessController {

    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createBusiness(@RequestBody Business business, Principal principal){
        String status = businessService.createBusiness(principal, business.getName(), business.getSlug(),
                business.getEmail(), business.getPhone(), business.getAddress(), business.getLogo() );
        return new ResponseEntity<>(status, HttpStatus.CREATED);
    }

    @PutMapping()
    public ResponseEntity<String> EditBusiness(@RequestBody BusinessDto businessDto, Principal principal){

        String status = businessService.editBusiness(businessDto.getName(),businessDto.getSlug()
                , businessDto.getEmail(), businessDto.getPhone(), businessDto.getAddress(), businessDto.getLogo(), principal );

        return new ResponseEntity<>(status, HttpStatus.OK);

    }

    @DeleteMapping()
    public ResponseEntity<String> DeleteBusiness(Principal principal){

        String status = businessService.deleteBusiness(principal);
        return new ResponseEntity<>(status, HttpStatus.OK);
    }
}
