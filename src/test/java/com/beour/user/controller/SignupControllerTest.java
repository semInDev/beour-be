package com.beour.user.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.beour.global.validator.implement.LoginIdValidator;
import com.beour.global.validator.implement.NicknameValidator;
import com.beour.user.service.SignupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SignupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 회원가입 테스트 - 성공
     * 아이디 중복 -> 중복일 경우
     * 중복이 아닐 경우
     * 아이디 유효성 검증
     * 닉네임 유효성 검증
     * 닉네임 중복일 경우
     * 닉네임 중복 검증 성공 테스트
     */



}