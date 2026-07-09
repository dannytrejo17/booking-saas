package com.gestorReservas.Service;

import com.gestorReservas.Dto.BookingDto;
import com.gestorReservas.Dto.BusinessDto;
import com.gestorReservas.Model.Booking;
import com.gestorReservas.Model.Business;
import com.gestorReservas.Model.User;
import com.gestorReservas.Repository.BusinessRepository;
import com.gestorReservas.Repository.ServiceRepository;
import com.gestorReservas.Repository.UserRepository;
import com.gestorReservas.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;


    public BusinessService(BusinessRepository businessRepository, UserRepository userRepository, ServiceRepository serviceRepository) {
        this.businessRepository = businessRepository;
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;

    }

    public String createBusiness(Principal principal, String name, String slug,
                                 String email, String phone, String address, String logo) {

        User owner = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "usuario no encontrado"));

        validateCreateBusiness(owner, name, slug, email);

        Business business = new Business();
        business.setUser(owner);
        business.setName(name.trim());
        business.setSlug(slug.trim().toLowerCase());
        business.setEmail(email.trim());
        business.setPhone(phone);
        business.setAddress(address);
        business.setLogo(logo);
        businessRepository.save(business);

        owner.setBusiness(business);
        userRepository.save(owner);

        return "negocio creado";
    }

    private void validateCreateBusiness(User owner, String name, String slug, String email) {
        if (isBlank(name) || isBlank(slug) || isBlank(email)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "name, slug y email son obligatorios");
        }

        if (owner.getBusiness() != null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ya tienes un negocio creado");
        }

        String normalizedSlug = slug.trim().toLowerCase();
        if (businessRepository.existsBySlug(normalizedSlug)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "el slug ya esta en uso");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public String editBusiness(String name, String slug, String email,
                               String phone, String address, String logo, Principal principal) {

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "no autenticado"));

        Business business = user.getBusiness();
        if(business == null){
            throw new ApiException(HttpStatus.BAD_REQUEST, "no tienes negocio");
        }

        business.setName(name);
        business.setSlug(slug);
        business.setEmail(email);
        business.setPhone(phone);
        business.setAddress(address);
        business.setLogo(logo);
        businessRepository.save(business);

        return "negocio editado";
    }

    public String deleteBusiness(Principal principal){

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "no autenticado"));

        Business business = user.getBusiness();
        if(business == null){
            throw new ApiException(HttpStatus.BAD_REQUEST, "no tienes negocio");
        }

        List<com.gestorReservas.Model.Service> service = serviceRepository.findByBusiness_BusinessId(business.getBusinessId());

        if(!service.isEmpty()){
            throw new ApiException(HttpStatus.BAD_REQUEST, "elimina los servicios primero");
        }

        user.setBusiness(null);
        userRepository.save(user);
        businessRepository.delete(business);

        return "negocio eliminado";
    }


    public BusinessDto getBusinessBySlug(String slug) {
        Business business = businessRepository.findBySlug(slug.trim().toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "negocio no encontrado"));
        return BusinessDto.from(business);
    }



}
