package com.gestorReservas.Service;

import com.gestorReservas.Model.Business;
import com.gestorReservas.Model.User;
import com.gestorReservas.Repository.BusinessRepository;
import com.gestorReservas.Repository.ServiceRepository;
import com.gestorReservas.Repository.UserRepository;
import com.gestorReservas.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessServiceTest {

    @Mock
    private BusinessRepository businessRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private BusinessService businessService;

    private Principal principal(String email) {
        return () -> email;
    }

    @Test
    void createBusiness_creaNegocioCorrectamente() {
        User owner = new User();
        owner.setEmail("owner@test.com");
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(businessRepository.existsBySlug("mi-negocio")).thenReturn(false);

        String result = businessService.createBusiness(
                principal("owner@test.com"),
                "Mi Negocio", "Mi-Negocio", "negocio@test.com", "600000000", "Calle 1", "logo.png"
        );

        assertEquals("negocio creado", result);
        verify(businessRepository).save(any(Business.class));
        verify(userRepository).save(owner);
    }

    @Test
    void createBusiness_usuarioYaTieneNegocio_lanzaBadRequest() {
        User owner = new User();
        owner.setEmail("owner@test.com");
        owner.setBusiness(new Business());
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));

        ApiException ex = assertThrows(ApiException.class, () ->
                businessService.createBusiness(
                        principal("owner@test.com"),
                        "Mi Negocio", "slug", "negocio@test.com", null, null, null
                ));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("ya tienes un negocio creado", ex.getMessage());
    }

    @Test
    void createBusiness_slugDuplicado_lanzaBadRequest() {
        User owner = new User();
        owner.setEmail("owner@test.com");
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(businessRepository.existsBySlug("slug")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () ->
                businessService.createBusiness(
                        principal("owner@test.com"),
                        "Mi Negocio", "slug", "negocio@test.com", null, null, null
                ));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("el slug ya esta en uso", ex.getMessage());
    }

    @Test
    void createBusiness_camposObligatoriosVacios_lanzaBadRequest() {
        User owner = new User();
        owner.setEmail("owner@test.com");
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));

        ApiException ex = assertThrows(ApiException.class, () ->
                businessService.createBusiness(
                        principal("owner@test.com"),
                        "", "slug", "negocio@test.com", null, null, null
                ));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("name, slug y email son obligatorios", ex.getMessage());
    }

    @Test
    void deleteBusiness_conServicios_lanzaBadRequest() {
        User user = new User();
        Business business = new Business();
        business.setBusinessId(1L);
        user.setBusiness(business);
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(user));
        when(serviceRepository.findByBusiness_BusinessId(1L))
                .thenReturn(List.of(new com.gestorReservas.Model.Service()));

        ApiException ex = assertThrows(ApiException.class, () ->
                businessService.deleteBusiness(principal("owner@test.com")));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("elimina los servicios primero", ex.getMessage());
    }

    @Test
    void deleteBusiness_sinServicios_eliminaCorrectamente() {
        User user = new User();
        Business business = new Business();
        business.setBusinessId(1L);
        user.setBusiness(business);
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(user));
        when(serviceRepository.findByBusiness_BusinessId(1L)).thenReturn(Collections.emptyList());

        String result = businessService.deleteBusiness(principal("owner@test.com"));

        assertEquals("negocio eliminado", result);
        verify(businessRepository).delete(business);
        verify(userRepository).save(user);
    }
}
