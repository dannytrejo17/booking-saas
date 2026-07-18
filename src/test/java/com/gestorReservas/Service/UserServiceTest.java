package com.gestorReservas.Service;

import com.gestorReservas.Dto.UserDto;
import com.gestorReservas.Model.User;
import com.gestorReservas.Repository.UserRepository;
import com.gestorReservas.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.security.Principal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Principal principal;

    @InjectMocks
    private UserService userService;

    @Test
    void me_devuelveUsuario() {
        User user = new User();
        user.setId(1L);
        user.setName("Juan");
        user.setEmail("juan@test.com");

        when(principal.getName()).thenReturn("juan@test.com");
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(user));

        UserDto dto = userService.Me(principal);

        assertEquals(1L, dto.getId());
        assertEquals("Juan", dto.getName());
        assertEquals("juan@test.com", dto.getEmail());
    }

    @Test
    void me_usuarioNoEncontrado_lanzaUnauthorized() {
        when(principal.getName()).thenReturn("nadie@test.com");
        when(userRepository.findByEmail("nadie@test.com")).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> userService.Me(principal));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertEquals("usuario no encontrado", ex.getMessage());
    }
}
