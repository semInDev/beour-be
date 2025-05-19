package com.beour.user.controller;

import com.beour.global.exception.exceptionType.UserNotFoundException;
import com.beour.user.dto.FindLoginIdRequestDto;
import com.beour.user.dto.FindLoginIdResponseDto;
import com.beour.user.dto.ResetPasswordRequestDto;
import com.beour.user.dto.ResetPasswordResponseDto;
import com.beour.user.service.LoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class LoginController {

    private final LoginService loginService;

    @PostMapping("/api/users/find-login-id")
    public ResponseEntity<FindLoginIdResponseDto> findLoginId(@Valid @RequestBody FindLoginIdRequestDto dto){
        String loginId = loginService.findLoginId(dto);

        if(loginId == null){
            throw new UserNotFoundException("일치하는 회원이 없습니다.");
        }

        return ResponseEntity.ok(new FindLoginIdResponseDto(loginId, "해당 유저의 아이디입니다."));
    }

    @PostMapping("/api/users/reset-pw")
    public ResponseEntity<ResetPasswordResponseDto> resetPassword(@Valid @RequestBody ResetPasswordRequestDto dto){
        String tempPassword = loginService.resetPassword(dto);

        if(tempPassword ==  null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResetPasswordResponseDto(
                    null,
                    "일치하는 회원이 없습니다."
            ));
        }

        return ResponseEntity.ok(new ResetPasswordResponseDto(
                tempPassword,
                "임시 비밀번호가 발급되었습니다. 로그인 후 변경해 주세요."
        ));
    }

}
