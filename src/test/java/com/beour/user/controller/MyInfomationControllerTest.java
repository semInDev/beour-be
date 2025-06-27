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

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class MyInfomationControllerTest {

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JWTUtil jwtUtil;
    @Autowired
    private MockMvc mockMvc;

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


    /**
     * 메인 정보 조회
     * 세부 정보 조회
     * <p>
     * 닉네임 중복일 경우
     * 내 정보 수정 완료
     * <p>
     * 비번 수정 완료
     */

    @Test
    @DisplayName("메인 정보 조회 - 성공")
    void update_user_info_blank() throws Exception {
        //when //then
        mockMvc.perform(get("/api/mypage")
                .header("Authorization", "Bearer " + accessToken)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.userName").value(savedUser.getName()))
            .andExpect(jsonPath("$.data.userEmail").value(savedUser.getEmail()));
    }

}