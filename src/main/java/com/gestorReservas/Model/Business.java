package com.gestorReservas.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long businessId;

    @ManyToOne
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    private User user;
    private String name;
    private String slug;
    private String email;
    private String phone;
    private String address;
    private String logo;
    private String coverImage;
    private LocalDateTime created_at;
    private LocalDateTime updatedAt;
}
