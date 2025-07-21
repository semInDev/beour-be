package com.beour.space.host.controller;

import com.beour.global.response.ApiResponse;
import com.beour.space.host.dto.AvailableTimeDetailResponseDto;
import com.beour.space.host.dto.AvailableTimeUpdateRequestDto;
import com.beour.space.host.service.AvailableTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/host/available-times")
public class AvailableTimeController {

    private final AvailableTimeService availableTimeService;

    @GetMapping("/space/{spaceId}")
    public ApiResponse<AvailableTimeDetailResponseDto> getAvailableTimeDetail(@PathVariable Long spaceId) {
        return ApiResponse.ok(availableTimeService.getAvailableTimeDetail(spaceId));
    }

    @PatchMapping("/space/{spaceId}")
    public ApiResponse<String> updateAvailableTimes(@PathVariable Long spaceId,
                                                    @RequestBody AvailableTimeUpdateRequestDto requestDto) {
        availableTimeService.updateAvailableTimes(spaceId, requestDto);
        return ApiResponse.ok("대여 가능 시간을 성공적으로 업데이트 했습니다.");
    }
}
