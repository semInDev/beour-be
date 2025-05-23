package com.beour.reservation.guest.controller;

import com.beour.global.response.ApiResponse;
import com.beour.reservation.guest.dto.CheckAvailableTimesRequestDto;
import com.beour.reservation.guest.dto.ReservationCreateRequest;
import com.beour.reservation.guest.dto.ReservationResponseDto;
import com.beour.reservation.guest.dto.SpaceAvailableTimeResponseDto;
import com.beour.reservation.guest.service.CheckAvailableTimeService;
import com.beour.reservation.guest.service.ReservationGuestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class ReservationGuestController {

    private final ReservationGuestService reservationGuestService;
    private final CheckAvailableTimeService checkAvailableTimeService;

    @PostMapping("/api/spaces/reserve")
    public ApiResponse<ReservationResponseDto> createReservation(@Valid @RequestBody ReservationCreateRequest requestDto){
        return ApiResponse.ok(reservationGuestService.createReservation(requestDto));
    }

    @PostMapping("/api/spaces/reserve/available-times")
    public ApiResponse<SpaceAvailableTimeResponseDto> checkAvailableTimes(@Valid @RequestBody CheckAvailableTimesRequestDto requestDto){
        return ApiResponse.ok(checkAvailableTimeService.findAvailableTime(requestDto));
    }

}
