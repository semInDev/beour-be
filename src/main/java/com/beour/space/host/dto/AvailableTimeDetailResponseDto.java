package com.beour.space.host.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
public class AvailableTimeDetailResponseDto {
    private Long spaceId;
    private List<TimeSlot> editableTimeSlots;
    private List<TimeSlot> nonEditableTimeSlots;

    @Getter
    @Builder
    public static class TimeSlot {
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
    }
}
