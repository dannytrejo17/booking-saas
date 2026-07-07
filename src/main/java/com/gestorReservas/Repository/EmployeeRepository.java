package com.gestorReservas.Repository;

import com.gestorReservas.Model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByBusinessBusinessId(Long businessId);

}
