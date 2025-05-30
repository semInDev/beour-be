package com.beour.user.controller;

import com.beour.global.response.ApiResponse;
import com.beour.user.dto.UpdateUserInfoRequestDto;
import com.beour.user.dto.UserInformationDetailResponseDto;
import com.beour.user.dto.UserInformationSimpleResponseDto;
import com.beour.user.service.MyInformationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ApiResponse<UserInformationDetailResponseDto> readDetailInformation(){
        return ApiResponse.ok(myInformationService.getUserInformationDetail());
    }

    @PatchMapping("/detail")
    public ApiResponse<String> updateUserInformation(@Valid @RequestBody UpdateUserInfoRequestDto requestDto){
        myInformationService.updateUserInfo(requestDto);

        return ApiResponse.ok("수정이 완료되었습니다.");
    }



}
