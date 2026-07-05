package com.gestorReservas.Controller;

import com.gestorReservas.Model.User;
import com.gestorReservas.Service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user){
        String status = userService.register(user.getName(), user.getEmail(), user.getPassword());
        return new ResponseEntity<>(status, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user){
        String status = userService.login(user.getEmail(), user.getPassword());
        return new ResponseEntity<>(status,HttpStatus.OK);
    }

}
