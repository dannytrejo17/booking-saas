package com.gestorReservas.Dto;

import com.gestorReservas.Model.Business;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessDto {

    private Long businessId;
    private String name;
    private String slug;
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
