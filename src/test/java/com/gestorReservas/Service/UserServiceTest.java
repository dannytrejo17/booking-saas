package com.gestorReservas.Service;

import com.gestorReservas.Model.User;
import com.gestorReservas.Repository.UserRepository;
import com.gestorReservas.exception.ApiException;
import com.gestorReservas.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    @Test
    void register_creaUsuarioCorrectamente() {
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");

        String result = userService.register("Juan", "juan@test.com", "password123");

        assertEquals("usuario creado", result);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("Juan", captor.getValue().getName());
        assertEquals("juan@test.com", captor.getValue().getEmail());
        assertEquals("encoded-password", captor.getValue().getPassword());
    }

    @Test
    void register_emailDuplicado_lanzaBadRequest() {
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(new User()));

        ApiException ex = assertThrows(ApiException.class,
                () -> userService.register("Juan", "juan@test.com", "password123"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("No se pudo completar el registro", ex.getMessage());
    }

    @Test
    void login_credencialesCorrectas_devuelveToken() {
        User user = new User();
        user.setEmail("juan@test.com");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        String token = userService.login("juan@test.com", "password123");

        assertEquals("jwt-token", token);
    }

    @Test
    void login_credencialesIncorrectas_lanzaUnauthorized() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));

        ApiException ex = assertThrows(ApiException.class,
                () -> userService.login("juan@test.com", "wrong"));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertEquals("credenciales incorrectas", ex.getMessage());
    }
}
