package com.gestorReservas.Dto;

import com.gestorReservas.Model.BusinessSchedule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleDto {
    private Long id;
    private DayOfWeek dayOfWeek;
    private LocalTime openTime;
    private LocalTime closeTime;

    public static ScheduleDto from(BusinessSchedule s) {
        return new ScheduleDto(
                s.getId(),
                s.getDayOfWeek(),
                s.getOpenTime(),
                s.getCloseTime()
        );
    }
}