package com.beour.reservation.guest.controller;

import com.beour.global.response.ApiResponse;
import com.beour.reservation.guest.dto.ReservationCreateRequest;
import com.beour.reservation.guest.dto.ReservationResponseDto;
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

    @PostMapping("api/spaces/reserve")
    public ApiResponse<ReservationResponseDto> createReservation(@Valid @RequestBody ReservationCreateRequest request){
        return ApiResponse.ok(reservationGuestService.createReservation(request));
    }

}
