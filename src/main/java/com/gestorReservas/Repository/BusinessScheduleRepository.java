package com.gestorReservas.Repository;

import com.gestorReservas.Model.Business;
import com.gestorReservas.Model.BusinessSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public interface BusinessScheduleRepository extends JpaRepository<BusinessSchedule, Long> {

    boolean existsByBusinessAndDayOfWeekAndOpenTimeAndCloseTime(
            Business business, DayOfWeek dayOfWeek, LocalTime openTime, LocalTime closeTime);


    List<BusinessSchedule> findByBusiness_BusinessId(Long businessId);


    List<BusinessSchedule> findByBusiness_BusinessIdAndDayOfWeek(Long businessId, DayOfWeek dayOfWeek);


    List<BusinessSchedule> findByDayOfWeekAndBusiness_BusinessId(DayOfWeek dayOfWeek, Long id);
}
