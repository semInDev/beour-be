package com.beour.reservation.host.controller;

import com.beour.global.response.ApiResponse;
import com.beour.reservation.host.dto.CalendarReservationPageResponseDto;
import com.beour.reservation.host.service.ReservationCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RequiredArgsConstructor
@RestController
public class ReservationCalendarController {

    private final ReservationCalendarService reservationCalendarService;

    @GetMapping("/api/reservations/condition")
    public ApiResponse<CalendarReservationPageResponseDto> getHostCalendarReservations(
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "spaceId", required = false) Long spaceId,
            @RequestParam(value = "status", required = false) String status,
            @PageableDefault(size = 10, sort = "startTime", direction = Sort.Direction.ASC) Pageable pageable) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now();

        if ("pending".equals(status)) {
            return ApiResponse.ok(reservationCalendarService.getHostPendingReservations(targetDate, spaceId, pageable));
        } else if ("accepted".equals(status)) {
            return ApiResponse.ok(reservationCalendarService.getHostAcceptedReservations(targetDate, spaceId, pageable));
        } else {
            return ApiResponse.ok(reservationCalendarService.getHostCalendarReservations(targetDate, spaceId, pageable));
        }
    }

    @PatchMapping("/api/reservations/{reservationId}/accept")
    public ApiResponse<String> acceptReservation(
            @PathVariable("reservationId") Long reservationId,
            @RequestParam(value = "spaceId") Long spaceId) {
        reservationCalendarService.acceptReservation(reservationId, spaceId);
        return ApiResponse.ok("예약이 승인되었습니다.");
    }

    @PatchMapping("/api/reservations/{reservationId}/reject")
    public ApiResponse<String> rejectReservation(
            @PathVariable("reservationId") Long reservationId,
            @RequestParam(value = "spaceId") Long spaceId) {
        reservationCalendarService.rejectReservation(reservationId, spaceId);
        return ApiResponse.ok("예약이 거부되었습니다.");
    }
}
