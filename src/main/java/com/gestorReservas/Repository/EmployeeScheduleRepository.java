package com.gestorReservas.Repository;

import com.gestorReservas.Model.EmployeeSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.DayOfWeek;
import java.util.List;

public interface EmployeeScheduleRepository extends JpaRepository<EmployeeSchedule, Long> {
    List<EmployeeSchedule> findByEmployee_IdAndDayOfWeek(Long employeeId, DayOfWeek dayOfWeek);

    List<EmployeeSchedule> findByEmployee_Id(Long employeeId);

    @Query("""
    SELECT s FROM EmployeeSchedule s
    JOIN FETCH s.employee e
    WHERE e.business.businessId = :businessId
      AND e.active = true
      AND s.dayOfWeek = :dayOfWeek
    """)
List<EmployeeSchedule> findActiveSchedulesOnDay(
        @Param("businessId") Long businessId,
        @Param("dayOfWeek") DayOfWeek dayOfWeek
);
}
