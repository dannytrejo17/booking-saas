package com.gestorReservas.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "business_id", referencedColumnName = "businessId")
    private Business business;
    private String name;
    private String email;
    private String password;
    private String role;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

}
