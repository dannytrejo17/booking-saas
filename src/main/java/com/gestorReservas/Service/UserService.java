package com.gestorReservas.Service;

import com.gestorReservas.Model.User;
import com.gestorReservas.Repository.UserRepository;
import com.gestorReservas.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty() || !passwordEncoder.matches(password, user.get().getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        return "login exitoso";
    }
}
