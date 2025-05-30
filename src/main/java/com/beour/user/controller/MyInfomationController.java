package com.beour.user.controller;

import com.beour.global.response.ApiResponse;
import com.beour.user.dto.UserInformationSimpleResponseDto;
import com.beour.user.service.MyInformationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/mypage")
public class MyInfomationController {

    private final MyInformationService myInformationService;

    @GetMapping
    public ApiResponse<UserInformationSimpleResponseDto> readInformation(){
        return ApiResponse.ok(myInformationService.getUserInformationSimple());
    }

    @GetMapping("/detail")
    public ApiResponse<UserInformationSimpleResponseDto> readDetailInformation(){
        return ApiResponse.ok(myInformationService.getUserInformationSimple());
    }


}
