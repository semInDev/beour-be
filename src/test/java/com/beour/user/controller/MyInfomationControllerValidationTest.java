package com.beour.user.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.beour.global.jwt.JWTUtil;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class MyInfomationControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JWTUtil jwtUtil;

    private User savedUser;
    private String accessToken;

    @BeforeEach
    void setUp() {
        savedUser = User.builder()
            .loginId("testUser")
            .password(passwordEncoder.encode("oldpassword!"))
            .name("테스트")
            .nickname("test")
            .email("test@gmail.com")
            .phone("01012345678")
            .role("GUEST")
            .build();

        userRepository.save(savedUser);

        // 2. 토큰 생성
        accessToken = jwtUtil.createJwt(
            "access",
            savedUser.getLoginId(),
            "ROLE_" + savedUser.getRole(),
            1000L * 60 * 30    // 30분
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("내 정보 수정 - 닉네임, 핸드폰 공백")
    void update_user_info_blank() throws Exception {
        //given
        String requestJson = """
                {
                    "newNickname": "",
                    "newPhone": ""
                }
            """;

        //when //then
        mockMvc.perform(patch("/api/mypage/detail")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("수정할 정보를 입력해주세요."));
    }

    @Test
    @DisplayName("내 정보 수정 - 닉네임 9자리")
    void update_user_info_nickname_length_nine() throws Exception {
        //given
        String requestJson = """
                {
                    "newNickname": "123456789",
                    "newPhone": ""
                }
            """;

        //when //then
        mockMvc.perform(patch("/api/mypage/detail")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("닉네임은 1자 이상 8자 이하여야 합니다."));
    }

    @Test
    @DisplayName("내 정보 수정 - 핸드폰 9자리")
    void update_user_info_phonenum_length_nine() throws Exception {
        //given
        String requestJson = """
                {
                    "newNickname": "",
                    "newPhone": "123456789"
                }
            """;

        //when //then
        mockMvc.perform(patch("/api/mypage/detail")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("전화번호는 숫자만 10~11자리로 입력하세요."));
    }

    @Test
    @DisplayName("내 정보 수정 - 핸드폰 12자리")
    void update_user_info_phonenum_length_twelve() throws Exception {
        //given
        String requestJson = """
                {
                    "newNickname": "",
                    "newPhone": "123456789012"
                }
            """;

        //when //then
        mockMvc.perform(patch("/api/mypage/detail")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("전화번호는 숫자만 10~11자리로 입력하세요."));
    }

    @Test
    @DisplayName("비밀번호 수정 - 비밀번호 공백")
    void update_user_info_password_blank() throws Exception {
        //given
        String requestJson = """
                {
                    "newPassword": ""
                }
            """;

        //when //then
        mockMvc.perform(patch("/api/mypage/password")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("비밀번호는 8자 이상 20자 이하, 특수문자를 1개 이상 포함시켜야합니다."));
    }

    @Test
    @DisplayName("비밀번호 수정 - 특수문자 미포함")
    void update_user_info_password_without_special_char() throws Exception {
        //given
        String requestJson = """
                {
                    "newPassword": "oldpassword"
                }
            """;

        //when //then
        mockMvc.perform(patch("/api/mypage/password")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("비밀번호는 8자 이상 20자 이하, 특수문자를 1개 이상 포함시켜야합니다."));
    }

    @Test
    @DisplayName("비밀번호 수정 - 길이 21자리")
    void update_user_info_password_length_twentyone() throws Exception {
        //given
        String requestJson = """
                {
                    "newPassword": "123456789012345678901"
                }
            """;

        //when //then
        mockMvc.perform(patch("/api/mypage/password")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("비밀번호는 8자 이상 20자 이하, 특수문자를 1개 이상 포함시켜야합니다."));
    }

    @Test
    @DisplayName("비밀번호 수정 - 길이 7자리")
    void update_user_info_password_length_seven() throws Exception {
        //given
        String requestJson = """
                {
                    "newPassword": "1234567"
                }
            """;

        //when //then
        mockMvc.perform(patch("/api/mypage/password")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("비밀번호는 8자 이상 20자 이하, 특수문자를 1개 이상 포함시켜야합니다."));
    }


}
