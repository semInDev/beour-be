package com.beour.user.controller;

import com.beour.user.dto.CheckDuplicateLoginIdDto;
import com.beour.user.dto.CheckDuplicateNickNameDto;
import com.beour.user.dto.CheckDuplicateResponse;
import com.beour.user.dto.SignupDto;
import com.beour.user.entity.User;
import com.beour.user.service.SignupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/users")
@RestController
public class SignupController {

    private final SignupService signupService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupDto signUpDto){
        try{
            User user = signupService.create(signUpDto);
            return ResponseEntity.ok("회원가입 완료");
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("회원가입 실패 : " + e.getMessage());
        }
    }

    @PostMapping("/signup/check/id")
    public ResponseEntity<CheckDuplicateResponse> checkDuplicateLoginId(@Valid @RequestBody CheckDuplicateLoginIdDto dto){
        if(signupService.checkIdDuplicate(dto)){
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new CheckDuplicateResponse(true, "이미 사용 중인 아이디입니다."));

        }

        return ResponseEntity.ok(new CheckDuplicateResponse(false, "사용 가능한 아이디입니다."));
    }

    @PostMapping("/signup/check/nickname")
    public ResponseEntity<CheckDuplicateResponse> checkDuplicateNickName(@Valid @RequestBody CheckDuplicateNickNameDto dto){
        if(signupService.checkNicknameDuplicate(dto)){
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new CheckDuplicateResponse(true, "이미 사용 중인 닉네임입니다."));

        }

        return ResponseEntity.ok(new CheckDuplicateResponse(false, "사용 가능한 닉네임입니다."));
    }
}
