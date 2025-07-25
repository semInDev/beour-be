package com.beour.reservation.guest.controller;

import com.beour.global.response.ApiResponse;
import com.beour.reservation.guest.dto.ReservationCreateRequest;
import com.beour.reservation.guest.dto.ReservationListPageResponseDto;
import com.beour.reservation.guest.dto.ReservationResponseDto;
import com.beour.reservation.guest.dto.SpaceAvailableTimeResponseDto;
import com.beour.reservation.guest.service.CheckAvailableTimeService;
import com.beour.reservation.guest.service.ReservationGuestService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class ReservationGuestController {

    private final ReservationGuestService reservationGuestService;
    private final CheckAvailableTimeService checkAvailableTimeService;

    @PostMapping("/api/spaces/{spaceId}/reservations")
    public ApiResponse<ReservationResponseDto> createReservation(
        @PathVariable(value = "spaceId") Long spaceId,
        @Valid @RequestBody ReservationCreateRequest requestDto) {
        return ApiResponse.ok(reservationGuestService.createReservation(spaceId, requestDto));
    }

    @GetMapping("/api/spaces/{spaceId}/available-times")
    public ApiResponse<SpaceAvailableTimeResponseDto> checkAvailableTimes(
        @PathVariable(value = "spaceId") Long spaceId, @RequestParam(value = "date")
    LocalDate date) {
        return ApiResponse.ok(checkAvailableTimeService.findAvailableTime(spaceId, date));
    }

    @GetMapping("/api/reservations/current")
    public ApiResponse<ReservationListPageResponseDto> checkReservationList(
        @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(reservationGuestService.findReservationList(pageable));
    }

    @GetMapping("/api/reservations/past")
    public ApiResponse<ReservationListPageResponseDto> checkPastReservationList(
        @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(reservationGuestService.findPastReservationList(pageable));
    }

    @DeleteMapping("/api/reservations/{reservationId}")
    public ApiResponse<String> cancelReservation(
        @PathVariable(value = "reservationId") Long reservationId) {
        reservationGuestService.cancelReservation(reservationId);
        return ApiResponse.ok("예약이 성공적으로 취소되었습니다.");
    }
}
