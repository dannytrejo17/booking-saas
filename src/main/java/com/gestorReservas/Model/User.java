package com.gestorReservas.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private Long id;
    private Business business;
    private String name;
    private String email;
    private String password;
    private String role;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

}
