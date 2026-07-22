package com.gestorReservas.Controller;

import com.gestorReservas.Dto.LoginRequest;
import com.gestorReservas.Dto.RegisterRequest;
import com.gestorReservas.Dto.ResendCodeRequest;
import com.gestorReservas.Dto.VerifyRequest;
import com.gestorReservas.Service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        String status = authService.register(registerRequest.getName(), registerRequest.getEmail(), registerRequest.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", status));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest loginRequest) {
        String token = authService.login(loginRequest.getEmail(), loginRequest.getPassword());
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/verifyCode")
    public ResponseEntity<Map<String, String>> verifyCode(@Valid @RequestBody VerifyRequest verifyRequest) {
        String status = authService.verifyCode(verifyRequest.getEmail(), verifyRequest.getCode());
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", status));
    }

    @PostMapping("/resendCode")
    public ResponseEntity<Map<String, String>> resendCode(@Valid @RequestBody ResendCodeRequest resendCodeRequest) {
        String status = authService.resendCode(resendCodeRequest.getEmail());
        return ResponseEntity.ok(Map.of("message", status));
    }
}
