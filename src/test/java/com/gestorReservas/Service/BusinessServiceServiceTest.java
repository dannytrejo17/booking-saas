package com.gestorReservas.Service;

import com.gestorReservas.Model.Business;
import com.gestorReservas.Model.Service;
import com.gestorReservas.Model.User;
import com.gestorReservas.Repository.ServiceRepository;
import com.gestorReservas.Repository.UserRepository;
import com.gestorReservas.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessServiceServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private BusinessServiceService businessServiceService;

    private Principal principal(String email) {
        return () -> email;
    }

    @Test
    void createService_sinNegocio_lanzaBadRequest() {
        User user = new User();
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(user));

        ApiException ex = assertThrows(ApiException.class, () ->
                businessServiceService.createService(
                        principal("owner@test.com"), "Corte", BigDecimal.TEN, 30));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("no tienes un negocio", ex.getMessage());
    }

    @Test
    void createService_precioCero_lanzaBadRequest() {
        User user = userWithBusiness();
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(user));

        ApiException ex = assertThrows(ApiException.class, () ->
                businessServiceService.createService(
                        principal("owner@test.com"), "Corte", BigDecimal.ZERO, 30));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("precio y duración deben ser mayores que 0", ex.getMessage());
    }

    @Test
    void createService_duracionCero_lanzaBadRequest() {
        User user = userWithBusiness();
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(user));

        ApiException ex = assertThrows(ApiException.class, () ->
                businessServiceService.createService(
                        principal("owner@test.com"), "Corte", BigDecimal.TEN, 0));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("precio y duración deben ser mayores que 0", ex.getMessage());
    }

    @Test
    void editProduct_deOtroNegocio_lanzaForbidden() {
        User user = userWithBusiness();
        Business otherBusiness = new Business();
        otherBusiness.setBusinessId(2L);
        Service service = new Service();
        service.setBusiness(otherBusiness);

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(user));
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));

        ApiException ex = assertThrows(ApiException.class, () ->
                businessServiceService.editProduct(
                        principal("owner@test.com"), "Corte", BigDecimal.TEN, 30, 1L));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("no tienes permiso", ex.getMessage());
    }

    private User userWithBusiness() {
        Business business = new Business();
        business.setBusinessId(1L);
        User user = new User();
        user.setBusiness(business);
        return user;
    }
}
