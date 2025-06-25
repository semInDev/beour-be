package com.beour.user.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
public class LoginControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     *
     * 이름
     * 이메일 공백, 형식
     * 핸드폰 특수문자
     * 핸드폰 공백
     *
     */

    @Test
    @DisplayName("이름 공란")
    void findLogId_invalid_name_blank() throws Exception {
        //given
        String requestJson = """
            {
                "name": "",
                "email": "test@gmail.com",
                "phone": "01012345678"
            }
        """;

        //when then
        mockMvc.perform(post("/api/users/find-login-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("이름은 필수입니다."));

    }

    @Test
    @DisplayName("이메일 공란")
    void findLogId_invalid_email_blank() throws Exception {
        //given
        String requestJson = """
            {
                "name": "유저1",
                "email": "",
                "phone": "01012345678"
            }
        """;

        //when then
        mockMvc.perform(post("/api/users/find-login-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("이메일은 필수입니다."));

    }

    @Test
    @DisplayName("이메일 형식 안맞음")
    void findLogId_invalid_email() throws Exception {
        //given
        String requestJson = """
            {
                "name": "유저1",
                "email": "abcgamilcom",
                "phone": "01012345678"
            }
        """;

        //when then
        mockMvc.perform(post("/api/users/find-login-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("올바른 이메일 형식이어야 합니다."));
    }

    @Test
    @DisplayName("핸드폰 번호 입력 특수문자 포함")
    void findLogId_invalid_phone() throws Exception {
        //given
        String requestJson = """
            {
                "name": "유저1",
                "email": "user1@gmail.com",
                "phone": "0101234567+"
            }
        """;

        //when then
        mockMvc.perform(post("/api/users/find-login-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("전화번호는 숫자만 10~11자리로 입력하세요."));
    }


}
