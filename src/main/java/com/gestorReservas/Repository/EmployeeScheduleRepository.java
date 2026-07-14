package com.gestorReservas.Repository;

import com.gestorReservas.Model.EmployeeSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface EmployeeScheduleRepository extends JpaRepository<EmployeeSchedule, Long> {
    List<EmployeeSchedule> findByEmployee_IdAndDayOfWeek(Long employeeId, DayOfWeek dayOfWeek);

    List<EmployeeSchedule> findByEmployee_Id(Long employeeId);
}
