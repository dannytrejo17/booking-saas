package com.gestorReservas.Controller;

import com.gestorReservas.Service.AuthService;
import com.gestorReservas.exception.ApiException;
import com.gestorReservas.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @BeforeEach
    void setUp() {
        AuthController controller = new AuthController(authService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void register_devuelve201YJson() throws Exception {
        when(authService.register(anyString(), anyString(), anyString())).thenReturn("usuario creado");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Juan","email":"juan@test.com","password":"password123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("usuario creado"));
    }

    @Test
    void login_devuelve200YToken() throws Exception {
        when(authService.login(anyString(), anyString())).thenReturn("jwt-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"juan@test.com","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void register_emailDuplicado_devuelve400() throws Exception {
        when(authService.register(anyString(), anyString(), anyString()))
                .thenThrow(new ApiException(HttpStatus.BAD_REQUEST, "No se pudo completar el registro"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Juan","email":"juan@test.com","password":"password123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No se pudo completar el registro"));
    }

    @Test
    void verifyCode_devuelve200YJson() throws Exception {
        when(authService.verifyCode(anyString(), anyString())).thenReturn("usuario verificado");

        mockMvc.perform(post("/api/auth/verifyCode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"juan@test.com","code":"123456"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("usuario verificado"));
    }
}
