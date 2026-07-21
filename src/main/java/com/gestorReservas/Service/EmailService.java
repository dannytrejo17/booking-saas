package com.gestorReservas.Service;

public interface EmailService {
    void sendVerificationCode(String toEmail, String code);
}
