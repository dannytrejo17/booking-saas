package com.gestorReservas.Controller;

import com.gestorReservas.Dto.UserDto;
import com.gestorReservas.Service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(Principal principal){
        UserDto user = userService.Me(principal);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }
}
