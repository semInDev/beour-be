package com.beour.user.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.beour.global.exception.error.errorcode.UserErrorCode;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SignupControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        User user = User.builder()
            .name("중복테스트")
            .nickname("duptest")
            .email("duptest@gmail.com")
            .loginId("duptest")
            .password("duptest")
            .phone("01012345678")
            .role("GUEST")
            .build();

        userRepository.save(user);
    }

    @Test
    @DisplayName("회원가입시 모든 유효성 만족")
    void success_signup() throws Exception {
        //given
        String requestJson = """
                {
                    "name": "test",
                    "nickname": "nicktest",
                    "email": "test@gmail.com",
                    "loginId": "testid",
                    "password": "test1234!",
                    "phone": "01012345678",
                    "role": "GUEST"
                }
            """;

        //when then
        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value("회원가입 완료"));
    }

    @Test
    @DisplayName("회원가입시 아이디가 중복이다.")
    void fail_signup_duplicate_loginId() throws Exception {
        //given
        String requestJson = """
                {
                    "name": "test",
                    "nickname": "nicktest",
                    "email": "test@gmail.com",
                    "loginId": "duptest",
                    "password": "test1234!",
                    "phone": "01012345678",
                    "role": "GUEST"
                }
            """;

        //when then
        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value(UserErrorCode.LOGIN_ID_DUPLICATE.getMessage()));
    }

    @Test
    @DisplayName("회원가입시 닉네임이 중복이다.")
    void fail_signup_duplicate_nickname() throws Exception {
        //given
        String requestJson = """
                {
                    "name": "test",
                    "nickname": "duptest",
                    "email": "test@gmail.com",
                    "loginId": "testid",
                    "password": "test1234!",
                    "phone": "01012345678",
                    "role": "GUEST"
                }
            """;

        //when then
        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value(UserErrorCode.NICKNAME_ID_DUPLICATE.getMessage()));
    }

    @Test
    @DisplayName("아이디가 중복된다.")
    void duplicate_loginId() throws Exception {
        //when then
        mockMvc.perform(get("/api/users/signup/check/loginId/duptest"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value(UserErrorCode.LOGIN_ID_DUPLICATE.getMessage()));
    }

    @Test
    @DisplayName("아이디를 사용할 수 있다.(중복X)")
    void not_duplicate_loginId() throws Exception {
        //when then
        mockMvc.perform(get("/api/users/signup/check/loginId/testId"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value("사용 가능한 아이디입니다."));
    }

    @Test
    @DisplayName("닉네임이 중복된다.")
    void duplicate_nickname() throws Exception {
        //when then
        mockMvc.perform(get("/api/users/signup/check/nickname/duptest"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value(UserErrorCode.NICKNAME_ID_DUPLICATE.getMessage()));
    }

    @Test
    @DisplayName("닉네임을 사용할 수 있다.(중복X)")
    void not_duplicate_nickname() throws Exception {
        //when then
        mockMvc.perform(get("/api/users/signup/check/nickname/testnick"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value("사용 가능한 닉네임입니다."));
    }
}