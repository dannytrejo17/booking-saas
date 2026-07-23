package com.gestorReservas.Repository;

import com.gestorReservas.Model.Booking;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBusinessBusinessId(Long businessId);

    List<Booking> findByEmployeeId(Long employeeId);

    @Query("""
        SELECT b FROM Booking b
        JOIN FETCH b.service
        WHERE b.employee.id = :employeeId
          AND b.startAt >= :dayStart
          AND b.startAt < :dayEnd
        """)
    List<Booking> findByEmployeeIdAndStartAtBetween(
            @Param("employeeId") Long employeeId,
            @Param("dayStart") LocalDateTime dayStart,
            @Param("dayEnd") LocalDateTime dayEnd
    );

    @Query("""
        SELECT b FROM Booking b
        JOIN FETCH b.service
        JOIN FETCH b.employee
        WHERE b.employee.id IN :employeeIds
          AND b.startAt >= :dayStart
          AND b.startAt < :dayEnd
        """)
    List<Booking> findByEmployeeIdInAndStartAtBetween(
            @Param("employeeIds") List<Long> employeeIds,
            @Param("dayStart") LocalDateTime dayStart,
            @Param("dayEnd") LocalDateTime dayEnd
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT b FROM Booking b
        JOIN FETCH b.service s
        WHERE b.employee.id = :employeeId
          AND b.startAt < :requestedEnd
          AND (:excludeId IS NULL OR b.id <> :excludeId)
        """)
    List<Booking> findOverlappingWithLock(
            @Param("employeeId") Long employeeId,
            @Param("requestedEnd") LocalDateTime requestedEnd,
            @Param("excludeId") Long excludeId
    );

    @Query("""
    SELECT COUNT(b) > 0 FROM Booking b
    WHERE b.business.businessId = :businessId
      AND b.customerPhone = :customerPhone
      AND b.startAt >= :dayStart
      AND b.startAt < :dayEnd
    """)
    boolean existsByBusinessAndPhoneOnDay(
            @Param("businessId") Long businessId,
            @Param("customerPhone") String customerPhone,
            @Param("dayStart") LocalDateTime dayStart,
            @Param("dayEnd") LocalDateTime dayEnd
    );
}
