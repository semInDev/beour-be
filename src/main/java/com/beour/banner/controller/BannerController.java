package com.beour.banner.controller;

import com.beour.banner.dto.BannerListForUserResponseDto;
import com.beour.banner.dto.BannerListResponseDto;
import com.beour.banner.dto.CreateBannerRequestDto;
import com.beour.banner.dto.CreateBannerResponseDto;
import com.beour.banner.service.BannerService;
import com.beour.global.response.ApiResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
public class BannerController {

    private final BannerService bannerService;

    @PostMapping(value = "/api/banners", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<CreateBannerResponseDto> createBanner(@Valid @RequestPart("banner")
    CreateBannerRequestDto requestDto, @RequestPart("file") MultipartFile file) throws IOException {
        return ApiResponse.ok(bannerService.createBanner(requestDto, file));
    }

    @GetMapping("/admin/banner/list")
    public ApiResponse<List<BannerListResponseDto>> getBannerList(){
        return ApiResponse.ok(bannerService.getBannerList());
    }

    @GetMapping("/api/banners")
    public ApiResponse<List<BannerListForUserResponseDto>> userGetBannerList(){
        return ApiResponse.ok(bannerService.getBannerListForUser());
    }

}
