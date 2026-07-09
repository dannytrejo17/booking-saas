package com.gestorReservas.Repository;

import com.gestorReservas.Model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBusinessBusinessId(Long businessId);

    List<Booking> findByEmployeeId(Long employeeId);
}
