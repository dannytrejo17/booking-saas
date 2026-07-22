package com.gestorReservas.Dto;

import com.gestorReservas.Model.Business;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessDto {

    private Long businessId;
    @NotBlank(message = "el nombre es requerido")
    @Size(min = 2 , max = 50, message = "el nombre debe tener entre 2 y 50 caracteres")
    private String name;
    @NotBlank(message = "el slug es requerido")
    @Size(min = 2 , max = 50, message = "el slug debe tener entre 2 y 50 caracteres")
    private String slug;
    @Email(message = "email invalido")
    private String email;
    private String phone;
    private String address;
    private String logo;
    private String coverImage;

    public static BusinessDto from(Business business) {
        if (business == null) {
            return null;
        }
        return new BusinessDto(
                business.getBusinessId(),
                business.getName(),
                business.getSlug(),
                business.getEmail(),
                business.getPhone(),
                business.getAddress(),
                business.getLogo(),
                business.getCoverImage()
        );
    }
}
