package com.beour.reservation.host.controller;

import com.beour.global.response.ApiResponse;
import com.beour.reservation.host.dto.CalendarReservationResponseDto;
import com.beour.reservation.host.service.ReservationCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class ReservationCalendarController {

    private final ReservationCalendarService reservationCalendarService;

    @GetMapping("/api/host/calendar/reservations")
    public ApiResponse<List<CalendarReservationResponseDto>> getHostCalendarReservations(
            @RequestParam(value = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.ok(reservationCalendarService.getHostCalendarReservations(date));
    }

    @GetMapping("/api/host/calendar/reservations/pending")
    public ApiResponse<List<CalendarReservationResponseDto>> getHostPendingReservations(
            @RequestParam(value = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.ok(reservationCalendarService.getHostPendingReservations(date));
    }

    @GetMapping("/api/host/calendar/reservations/accepted")
    public ApiResponse<List<CalendarReservationResponseDto>> getHostAcceptedReservations(
            @RequestParam(value = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.ok(reservationCalendarService.getHostAcceptedReservations(date));
    }
}
