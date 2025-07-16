package com.beour.user.controller;

import com.beour.global.response.ApiResponse;
import com.beour.global.validator.annotation.ValidLoginId;
import com.beour.global.validator.annotation.ValidNickname;
import com.beour.user.dto.SignupRequestDto;
import com.beour.user.service.SignupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequiredArgsConstructor
@RestController
public class SignupController {

    private final SignupService signupService;

    @PostMapping("/api/signup")
    public ApiResponse<String> signup(@RequestBody @Valid SignupRequestDto signUpRequestDto) {
        signupService.create(signUpRequestDto);
        return ApiResponse.ok("회원가입 완료");
    }

    @GetMapping("/api/signup/check-duplicate/loginId")
    public ApiResponse<String> checkDuplicateLoginId(
        @RequestParam("loginId") @ValidLoginId String loginId) {
        signupService.checkLoginIdDuplicate(loginId);
        return ApiResponse.ok("사용 가능한 아이디입니다.");
    }

    @GetMapping("/api/signup/check-duplicate/nickname")
    public ApiResponse<String> checkDuplicateNickName(
        @RequestParam("nickname") @ValidNickname String nickname) {
        signupService.checkNicknameDuplicate(nickname);
        return ApiResponse.ok("사용 가능한 닉네임입니다.");
    }
}
