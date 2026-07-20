package com.gestorReservas.security;

import com.gestorReservas.Model.User;
import com.gestorReservas.Repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtFilter jwtFilter;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_tokenValido_autenticaUsuario() throws Exception {
        User user = new User();
        user.setEmail("owner@test.com");

        when(jwtService.resolveToken(request)).thenReturn("valid.jwt");
        when(jwtService.extractUsername("valid.jwt")).thenReturn("owner@test.com");
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid("valid.jwt")).thenReturn(true);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertEquals("owner@test.com", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_tokenInvalido_noAutentica() throws Exception {
        when(jwtService.resolveToken(request)).thenReturn("bad.jwt");
        when(jwtService.extractUsername("bad.jwt")).thenReturn("owner@test.com");
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(new User()));
        when(jwtService.isTokenValid("bad.jwt")).thenReturn(false);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_sinToken_continuaSinAuth() throws Exception {
        when(jwtService.resolveToken(request)).thenReturn(null);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
