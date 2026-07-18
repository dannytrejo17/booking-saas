package com.gestorReservas.Controller;

import com.gestorReservas.Dto.VerifyRequest;
import com.gestorReservas.Model.User;
import com.gestorReservas.Service.AuthService;
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
    public ResponseEntity<Map<String, String>> register(@RequestBody User user) {
        String status = authService.register(user.getName(), user.getEmail(), user.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", status));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody User user) {
        String token = authService.login(user.getEmail(), user.getPassword());
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/verifyCode")
    public ResponseEntity<Map<String,String>> verifyCode(@RequestBody VerifyRequest verifyRequest){
        String status = authService.verifyCode(verifyRequest.getEmail(), verifyRequest.getCode());
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", status));
    }

}
