package com.gestorReservas.Controller;

import com.gestorReservas.Model.User;
import com.gestorReservas.Service.UserService;
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

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody User user) {
        String status = userService.register(user.getName(), user.getEmail(), user.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", status));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String,String>> login(@RequestBody User user){
        String token = userService.login(user.getEmail(), user.getPassword());
        return ResponseEntity.ok(Map.of("token", token));
    }

}
