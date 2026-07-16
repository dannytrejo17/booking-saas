package com.gestorReservas.Controller;

import com.gestorReservas.Dto.BusinessDto;
import com.gestorReservas.Model.Business;
import com.gestorReservas.Service.BusinessService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/business")
public class BusinessController {

    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String,String>> createBusiness(@RequestBody Business business, Principal principal){
        String status = businessService.createBusiness(principal, business.getName(), business.getSlug(),
                business.getEmail(), business.getPhone(), business.getAddress(), business.getLogo() );
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", status));
    }

    @PutMapping()
    public ResponseEntity<Map<String,String>> EditBusiness(@RequestBody BusinessDto businessDto, Principal principal){

        String status = businessService.editBusiness(businessDto.getName(),businessDto.getSlug()
                , businessDto.getEmail(), businessDto.getPhone(), businessDto.getAddress(), businessDto.getLogo(), principal );

        return ResponseEntity.ok(Map.of("message", status));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type,
            Principal principal) {
        return ResponseEntity.ok(businessService.uploadImage(file, type, principal));
    }

    @DeleteMapping()
    public ResponseEntity<Map<String,String>> DeleteBusiness(Principal principal){

        String status = businessService.deleteBusiness(principal);
        return ResponseEntity.ok(Map.of("message", status));

    }
}
