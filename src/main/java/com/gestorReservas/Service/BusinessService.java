package com.gestorReservas.Service;

import com.gestorReservas.Model.Business;
import com.gestorReservas.Model.User;
import com.gestorReservas.Repository.BusinessRespository;
import com.gestorReservas.Repository.UserRepository;
import com.gestorReservas.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
public class BusinessService {

    private final BusinessRespository businessRespository;
    private final UserRepository userRepository;

    public BusinessService(BusinessRespository businessRespository, UserRepository userRepository) {
        this.businessRespository = businessRespository;
        this.userRepository = userRepository;
    }

    public String createBusiness(Principal principal, String name, String slug,
                                 String email, String phone, String address, String logo) {

        if (principal == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "usuario no autenticado");
        }

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
        businessRespository.save(business);

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
        if (businessRespository.existsBySlug(normalizedSlug)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "el slug ya esta en uso");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }


}
