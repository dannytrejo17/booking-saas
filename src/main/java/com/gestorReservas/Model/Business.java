package com.gestorReservas.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Business {

    private Long businessId;
    private String name;
    private String slug;
    private String email;
    private String phone;
    private String address;
    private String logo;
    private LocalDateTime created_at;
    private LocalDateTime updatedAt;
}
