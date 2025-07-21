package com.beour.user.controller;

import com.beour.global.exception.error.errorcode.UserErrorCode;
import com.beour.global.jwt.JWTUtil;
import com.beour.token.entity.RefreshToken;
import com.beour.token.repository.RefreshTokenRepository;
import com.beour.user.entity.User;
import com.beour.user.enums.TokenExpireTime;
import com.beour.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class LogoutControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private JWTUtil jwtUtil;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        User user = User.builder().name("유저1").nickname("user1").email("user1@gmail.com")
            .loginId("user1").password(passwordEncoder.encode("user1")).phone("01012345678")
            .role("GUEST").build();
        userRepository.save(user);
    }

    @Test
    @DisplayName("로그아웃 - 성공")
    void success_logout() throws Exception {
        //given
        String loginId = "user1";
        String role = "GUEST";

        String refreshToken = jwtUtil.createJwt("refresh", loginId, role,
            TokenExpireTime.REFRESH_TOKEN_EXPIRATION_MILLIS.getValue());
        refreshTokenRepository.save(RefreshToken.builder().loginId(loginId).refresh(refreshToken)
            .expiration(String.valueOf(LocalDateTime.now().plusDays(1))).build());

        Cookie cookie = new Cookie("refresh", refreshToken);

        //when  then
        mockMvc.perform(post("/api/logout").cookie(cookie)).andExpect(status().isOk())
            .andExpect(cookie().value("refresh", (String) null))
            .andExpect(cookie().maxAge("refresh", 0));

        assertFalse(refreshTokenRepository.existsByRefresh(refreshToken));
    }

    @Test
    @DisplayName("로그아웃 - refresh 토큰 만료")
    void logout_refresh_token_expired() throws Exception {
        //given
        String loginId = "user1";
        String role = "GUEST";

        String refreshToken = jwtUtil.createJwt("refresh", loginId, role, 1L);
        refreshTokenRepository.save(RefreshToken.builder()
            .loginId(loginId)
            .refresh(refreshToken)
            .expiration(String.valueOf(LocalDateTime.now().plusDays(1)))
            .build());

        Cookie cookie = new Cookie("refresh", refreshToken);

        Thread.sleep(10);

        //when  then
        mockMvc.perform(post("/api/logout").cookie(cookie))
            .andExpect(status().isUnauthorized())
            .andExpect(
                jsonPath("$.message").value(UserErrorCode.REFRESH_TOKEN_EXPIRED.getMessage()));
    }

    @Test
    @DisplayName("로그아웃 - refresh 토큰이 아님")
    void logout_not_refresh_token() throws Exception {
        //given
        String loginId = "user1";
        String role = "GUEST";

        String refreshToken = jwtUtil.createJwt("access", loginId, role, TokenExpireTime.REFRESH_TOKEN_EXPIRATION_MILLIS.getValue());
        refreshTokenRepository.save(RefreshToken.builder()
            .loginId(loginId)
            .refresh(refreshToken)
            .expiration(String.valueOf(LocalDateTime.now().plusDays(1)))
            .build());

        Cookie cookie = new Cookie("refresh", refreshToken);

        //when  then
        mockMvc.perform(post("/api/logout").cookie(cookie))
            .andExpect(status().isNotFound())
            .andExpect(
                jsonPath("$.message").value(UserErrorCode.REFRESH_TOKEN_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("로그아웃 - refresh 토큰 없음")
    void logout_refresh_token_not_found() throws Exception {
        //when  then
        mockMvc.perform(post("/api/logout"))
            .andExpect(status().isNotFound())
            .andExpect(
                jsonPath("$.message").value(UserErrorCode.REFRESH_TOKEN_NOT_FOUND.getMessage()));
    }

}
