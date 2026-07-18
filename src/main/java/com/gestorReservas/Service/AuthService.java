package com.gestorReservas.Service;

import com.gestorReservas.Model.User;
import com.gestorReservas.Repository.UserRepository;
import com.gestorReservas.exception.ApiException;
import com.gestorReservas.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;


    public String register(String name, String email, String password) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No se pudo completar el registro");
        }
        User user1 = new User();
        user1.setName(name);
        user1.setEmail(email);
        user1.setPassword(passwordEncoder.encode(password));
        user1.setEnabled(false);
        assignVerificationCode(user1);
        userRepository.save(user1);
        emailService.sendVerificationCode(email,user1.getVerificationCode());
        return "usuario creado";
    }

    public String login(String email, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return jwtService.generateToken(userDetails);
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "credenciales incorrectas");
        } catch (DisabledException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "cuenta no verificada");
        }
    }

    private String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        int number = random.nextInt(1_000_000);
        return String.format("%06d", number);
    }

    private void assignVerificationCode(User user) {
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
    }

    public String verifyCode(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "usuario no encontrado"));

        if (user.getVerificationCode() == null
                || user.getVerificationCodeExpiresAt() == null
                || user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "codigo incorrecto o cuenta verificada");
        }

        if (!user.getVerificationCode().equals(code)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "codigo incorrecto");
        }

        user.setEnabled(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        userRepository.save(user);
        return "usuario verificado";
    }
}
