package com.beour.banner.controller;

import com.beour.banner.dto.CreateBannerRequestDto;
import com.beour.banner.dto.CreateBannerResponseDto;
import com.beour.banner.service.BannerService;
import com.beour.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/admin/banner")
@RestController
public class BannerController {

    private final BannerService bannerService;

    @PostMapping
    public ApiResponse<CreateBannerResponseDto> createBanner(@Valid @RequestBody
    CreateBannerRequestDto requestDto) {
        return ApiResponse.ok(bannerService.createBanner(requestDto));
    }


}
