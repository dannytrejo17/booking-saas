package com.gestorReservas.security;

import com.gestorReservas.Model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(
                jwtService,
                "SECRET_KEY",
                "test-jwt-secret-only-for-unit-tests-not-production"
        );
    }

    @Test
    void generateAndValidateToken_ok() {
        User user = new User();
        user.setEmail("owner@test.com");

        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token));
        assertEquals("owner@test.com", jwtService.extractUsername(token));
    }

    @Test
    void resolveToken_conBearer_devuelveToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer abc.def.ghi");

        assertEquals("abc.def.ghi", jwtService.resolveToken(request));
    }

    @Test
    void resolveToken_sinHeader_devuelveNull() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        assertNull(jwtService.resolveToken(request));
    }

    @Test
    void isTokenValid_tokenInvalido_lanzaExcepcion() {
        assertThrows(Exception.class, () -> jwtService.isTokenValid("token-invalido"));
    }

    @Test
    void extractUsername_tokenManipulado_falla() {
        User user = new User();
        user.setEmail("owner@test.com");
        String token = jwtService.generateToken(user);
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThrows(Exception.class, () -> jwtService.extractUsername(tampered));
    }
}
