package com.beour.reservation.host.controller;

import com.beour.global.response.ApiResponse;
import com.beour.reservation.host.dto.CalendarReservationResponseDto;
import com.beour.reservation.host.service.ReservationCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
            @RequestParam(value = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "spaceId", required = false) Long spaceId) {
        return ApiResponse.ok(reservationCalendarService.getHostCalendarReservations(date, spaceId));
    }

    @GetMapping("/api/host/calendar/reservations/pending")
    public ApiResponse<List<CalendarReservationResponseDto>> getHostPendingReservations(
            @RequestParam(value = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "spaceId", required = false) Long spaceId) {
        return ApiResponse.ok(reservationCalendarService.getHostPendingReservations(date, spaceId));
    }

    @GetMapping("/api/host/calendar/reservations/accepted")
    public ApiResponse<List<CalendarReservationResponseDto>> getHostAcceptedReservations(
            @RequestParam(value = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "spaceId", required = false) Long spaceId) {
        return ApiResponse.ok(reservationCalendarService.getHostAcceptedReservations(date, spaceId));
    }

    @PatchMapping("/api/host/calendar/reservations/accept")
    public ApiResponse<String> acceptReservation(
            @RequestParam(value = "reservationId") Long reservationId,
            @RequestParam(value = "spaceId") Long spaceId) {
        reservationCalendarService.acceptReservation(reservationId, spaceId);
        return ApiResponse.ok("예약이 승인되었습니다.");
    }

    @PatchMapping("/api/host/calendar/reservations/reject")
    public ApiResponse<String> rejectReservation(
            @RequestParam(value = "reservationId") Long reservationId,
            @RequestParam(value = "spaceId") Long spaceId) {
        reservationCalendarService.rejectReservation(reservationId, spaceId);
        return ApiResponse.ok("예약이 거부되었습니다.");
    }
}
