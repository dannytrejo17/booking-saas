package com.gestorReservas.Repository;

import com.gestorReservas.Model.Business;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessRepository extends JpaRepository<Business, Long> {

    boolean existsBySlug(String slug);
}
