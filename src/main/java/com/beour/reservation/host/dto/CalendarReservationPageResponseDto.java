package com.beour.reservation.host.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CalendarReservationPageResponseDto {
    private List<CalendarReservationResponseDto> reservations;
    private boolean last;
    private int totalPage;
}
