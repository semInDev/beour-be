//package com.beour.user.controller;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@ActiveProfiles("test")
//class SignupControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    /**
//     * 아이디 중복 -> 중복일 경우
//     * 중복이 아닐 경우
//     * 아이디 유효성 검증
//     * 닉네임 유효성 검증
//     * 닉네임 중복일 경우
//     * 닉네임 중복 검증 성공 테스트
//     */
//    @BeforeEach
//    void setup(){
//
//    }
//
//    @Test
//    @DisplayName("회원가입시 모든 유효성 만족")
//    void valid_request() throws Exception {
//        //given
//        String requestJson = """
//            {
//                "name": "test",
//                "nickname": "nicktest",
//                "email": "test@gmail.com",
//                "loginId": "testid",
//                "password": "test1234!",
//                "phone": "01012345678",
//                "role": "GUEST"
//            }
//        """;
//
//        //when then
//        mockMvc.perform(post("/api/users/signup")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(requestJson)
//            )
//            .andExpect(status().isOk());
//    }
//
//
//}