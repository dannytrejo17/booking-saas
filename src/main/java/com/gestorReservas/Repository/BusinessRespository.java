package com.gestorReservas.Repository;

import com.gestorReservas.Model.Business;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessRespository extends JpaRepository<Business, Long> {

    boolean existsBySlug(String slug);
}
