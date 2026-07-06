package com.gestorReservas.Service;

import com.gestorReservas.Model.User;
import com.gestorReservas.Repository.UserRepository;
import com.gestorReservas.exception.ApiException;
import com.gestorReservas.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public String register(String name, String email, String password) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No se pudo completar el registro");
        }
        User user1 = new User();
        user1.setName(name);
        user1.setEmail(email);
        user1.setPassword(passwordEncoder.encode(password));
        userRepository.save(user1);

        return "usuario creado";
    }

    public String login(String email, String password) {
        try{
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        password));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return jwtService.generateToken(userDetails);
    } catch (org.springframework.security.authentication.BadCredentialsException | DisabledException e) {
        throw new ApiException(HttpStatus.UNAUTHORIZED, "credenciales incorrectas");
    }



    }
}
