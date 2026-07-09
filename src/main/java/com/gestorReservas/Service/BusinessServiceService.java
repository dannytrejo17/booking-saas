package com.gestorReservas.Service;

import com.gestorReservas.Dto.ServiceDto;
import com.gestorReservas.Model.Business;
import com.gestorReservas.Model.Service;
import com.gestorReservas.Model.User;
import com.gestorReservas.Repository.BusinessRepository;
import com.gestorReservas.Repository.ServiceRepository;
import com.gestorReservas.Repository.UserRepository;
import com.gestorReservas.exception.ApiException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@org.springframework.stereotype.Service
public class BusinessServiceService {

    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final BusinessRepository businessRepository;


    public BusinessServiceService(UserRepository userRepository, ServiceRepository serviceRepository,
                                  BusinessRepository businessRepository) {
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.businessRepository = businessRepository;
    }

    public String createService(Principal principal, String name, BigDecimal price, int duration){

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(()-> new ApiException(HttpStatus.UNAUTHORIZED, "no autenticado"));

        if (user.getBusiness() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "no tienes un negocio");
        }

        if (price == null || price.signum() <= 0 || duration <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "precio y duración deben ser mayores que 0");
        }

        Service service = new Service();
        service.setBusiness(user.getBusiness());
        service.setName(name);
        service.setPrice(price);
        service.setDuration(duration);
        serviceRepository.save(service);
        return "servicio creado";
    }

    public List<ServiceDto> getAll(Principal principal) {

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "no autenticado"));

        Business business = user.getBusiness();
        if (business == null) {
            return Collections.emptyList();
        }

        List<Service> services = serviceRepository.findByBusiness_BusinessId(business.getBusinessId());
        List<ServiceDto> resultado = new ArrayList<>();
        for (Service service : services) {
            resultado.add(ServiceDto.from(service));
        }
        return resultado;
    }

    public List<ServiceDto> getAllBySlug(String slug) {
        Business business = businessRepository.findBySlug(slug.trim().toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "negocio no encontrado"));

        List<Service> services = serviceRepository.findByBusiness_BusinessId(business.getBusinessId());
        List<ServiceDto> resultado = new ArrayList<>();
        for (Service service : services) {
            resultado.add(ServiceDto.from(service));
        }
        return resultado;
    }

    public String editProduct(Principal principal,String name, BigDecimal price, int duration, Long id){

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "no autenticado"));

        if(user.getBusiness() == null){
            throw new ApiException(HttpStatus.BAD_REQUEST, "no tienes un negocio");
        }

        Service service = serviceRepository.findById(id)
                .orElseThrow(()-> new ApiException(HttpStatus.NOT_FOUND, "servicio no encontrado"));

        if(!service.getBusiness().getBusinessId().equals(user.getBusiness().getBusinessId())){
            throw new ApiException(HttpStatus.FORBIDDEN, "no tienes permiso");
        }

        if (price == null || price.signum() <= 0 || duration <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "precio y duración deben ser mayores que 0");
        }

        service.setName(name);
        service.setPrice(price);
        service.setDuration(duration);
        serviceRepository.save(service);

        return "servicio modificado";
    }

    public String deleteservice(Principal principal, Long id){

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "no autenticado"));

        if (user.getBusiness() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "no tienes un negocio");
        }

        Service service = serviceRepository.findById(id)
                .orElseThrow(()-> new ApiException(HttpStatus.NOT_FOUND, "servicio no encontrado"));

        if(!service.getBusiness().getBusinessId().equals(user.getBusiness().getBusinessId())){
            throw new ApiException(HttpStatus.FORBIDDEN, "no tienes permiso");
        }

        serviceRepository.delete(service);

        return "servicio eliminado";
    }



}
