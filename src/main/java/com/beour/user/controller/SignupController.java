package com.beour.user.controller;

import com.beour.user.dto.SignupDto;
import com.beour.user.service.SignupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class SignupController {
    private final SignupService signupService;

    @PostMapping("/api/users/signup")
    public String signup(@Valid @RequestBody SignupDto signUpDto){
        signupService.create(signUpDto);

        return "끝";
    }
}
