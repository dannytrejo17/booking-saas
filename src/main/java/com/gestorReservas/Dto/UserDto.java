package com.gestorReservas.Dto;

import com.gestorReservas.Model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    private Long id;
    private BusinessDto business;
    private String name;
    private String email;
    private String role;

    public static UserDto from(User user) {
        return new UserDto(
                user.getId(),
                BusinessDto.from(user.getBusiness()),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}
