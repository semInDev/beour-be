package com.beour.space.host.dto;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
public class AvailableTimeUpdateRequestDto {
    private List<AvailableTimeSlot> availableTimes;

    @Getter
    public static class AvailableTimeSlot {
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
    }
}
