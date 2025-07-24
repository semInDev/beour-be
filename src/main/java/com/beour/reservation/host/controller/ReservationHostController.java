package com.beour.reservation.host.controller;

import com.beour.global.response.ApiResponse;
import com.beour.reservation.host.dto.HostReservationListPageResponseDto;
import com.beour.reservation.host.dto.HostSpaceListResponseDto;
import com.beour.reservation.host.service.ReservationHostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

    @GetMapping("/api/reservations")
    public ApiResponse<HostReservationListPageResponseDto> getHostReservationsByDate(
            @RequestParam(value = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PageableDefault(size = 10, sort = "startTime", direction = Sort.Direction.ASC) Pageable pageable) {

        // date가 "today"인 경우 오늘 날짜로 변환
        LocalDate targetDate = date;
        if ("today".equals(date.toString())) {
            targetDate = LocalDate.now();
        }

        return ApiResponse.ok(reservationHostService.getHostReservationsByDate(targetDate, pageable));
    }

    @GetMapping("/api/spaces/reservations")
    public ApiResponse<HostReservationListPageResponseDto> getHostReservationsByDateAndSpace(
            @RequestParam(value = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "spaceId") Long spaceId,
            @PageableDefault(size = 10, sort = "startTime", direction = Sort.Direction.ASC) Pageable pageable) {

        // date가 "today"인 경우 오늘 날짜로 변환
        LocalDate targetDate = date;
        if ("today".equals(date.toString())) {
            targetDate = LocalDate.now();
        }

        return ApiResponse.ok(reservationHostService.getHostReservationsByDateAndSpace(targetDate, spaceId, pageable));
    }
}
