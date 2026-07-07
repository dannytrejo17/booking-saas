package com.gestorReservas.Repository;

import com.gestorReservas.Model.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceRepository extends JpaRepository<Service, Long> {

    List<Service> findByBusiness_BusinessId(Long businessId);

    Optional<Service> findById(Long id);
}
