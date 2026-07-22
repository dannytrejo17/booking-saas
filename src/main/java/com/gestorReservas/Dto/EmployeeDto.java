package com.gestorReservas.Dto;

import com.gestorReservas.Model.Employee;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDto {

    private Long id;
    @NotBlank(message = "el nombre es requerido")
    @Size(min = 2, max = 50, message = "el nombre debe tener entre 2 y 50 caracteres")
    private String name;
    private boolean active;

    public static EmployeeDto from(Employee employee) {
        return new EmployeeDto(
                employee.getId(),
                employee.getName(),
                employee.isActive()
        );
    }
}