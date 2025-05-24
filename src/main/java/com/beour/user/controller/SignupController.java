package com.beour.user.controller;

import com.beour.global.exception.exceptionType.DuplicateUserInfoException;
import com.beour.global.response.ApiResponse;
import com.beour.user.dto.SignupRequestDto;
import com.beour.user.service.SignupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/users")
@RestController
public class SignupController {

  private final SignupService signupService;

  @PostMapping("/signup")
  public ApiResponse<String> signup(@Valid @RequestBody SignupRequestDto signUpRequestDto) {
    signupService.create(signUpRequestDto);
    return ApiResponse.ok("회원가입 완료");
  }

  @GetMapping("/signup/check/loginId")
  public ResponseEntity<String> checkDuplicateLoginId(@RequestParam("loginId") String loginId) {
    if (signupService.checkLoginIdDuplicate(loginId)) {
      throw new DuplicateUserInfoException("이미 사용중인 아이디 입니다.");
    }

    return ResponseEntity.ok("사용 가능한 아이디입니다.");
  }

  @GetMapping("/signup/check/nickname")
  public ResponseEntity<String> checkDuplicateNickName(@RequestParam("nickname") String nickname) {
    if (signupService.checkNicknameDuplicate(nickname)) {
      throw new DuplicateUserInfoException("이미 사용중인 닉네임입니다.");
    }

    return ResponseEntity.ok("사용 가능한 닉네임입니다.");
  }
}
