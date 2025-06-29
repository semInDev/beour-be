package com.beour.reservation.host.controller;

import com.beour.global.response.ApiResponse;
import com.beour.reservation.host.dto.HostReservationListResponseDto;
import com.beour.reservation.host.dto.HostSpaceListResponseDto;
import com.beour.reservation.host.service.ReservationHostService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class ReservationHostController {

    private final ReservationHostService reservationHostService;

    @GetMapping("/api/host/spaces")
    public ApiResponse<List<HostSpaceListResponseDto>> getHostSpaces() {
        return ApiResponse.ok(reservationHostService.getHostSpaces());
    }

    @GetMapping("/api/host/reservations")
    public ApiResponse<List<HostReservationListResponseDto>> getHostReservationsByDate(
            @RequestParam(value = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.ok(reservationHostService.getHostReservationsByDate(date));
    }

    @GetMapping("/api/host/reservations/space")
    public ApiResponse<List<HostReservationListResponseDto>> getHostReservationsByDateAndSpace(
            @RequestParam(value = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "spaceId") Long spaceId) {
        return ApiResponse.ok(reservationHostService.getHostReservationsByDateAndSpace(date, spaceId));
    }
}
