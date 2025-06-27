package com.beour.reservation.host.controller;

import com.beour.global.response.ApiResponse;
import com.beour.reservation.host.dto.HostSpaceListResponseDto;
import com.beour.reservation.host.service.ReservationHostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ReservationHostController {

    private final ReservationHostService reservationHostService;

    @GetMapping("/api/host/spaces")
    public ApiResponse<List<HostSpaceListResponseDto>> getHostSpaces(@RequestParam(value = "hostId") Long hostId) {
        return ApiResponse.ok(reservationHostService.getHostSpaces(hostId));
    }
}
