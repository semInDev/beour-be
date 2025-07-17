package com.beour.user.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.beour.global.exception.error.errorcode.UserErrorCode;
import com.beour.global.jwt.JWTUtil;
import com.beour.token.entity.RefreshToken;
import com.beour.token.repository.RefreshTokenRepository;
import com.beour.user.entity.User;
import com.beour.user.enums.TokenExpireTime;
import com.beour.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.AssertTrue;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoginControllerTest {

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
        User user = User.builder()
            .name("유저1")
            .nickname("user1")
            .email("user1@gmail.com")
            .loginId("user1")
            .password(passwordEncoder.encode("user1"))
            .phone("01012345678")
            .role("GUEST")
            .build();
        userRepository.save(user);

        User deleteUser = User.builder()
            .name("탈퇴유저")
            .nickname("delete1")
            .email("delete1@gmail.com")
            .loginId("delete1")
            .password("delete1")
            .phone("01012345678")
            .role("GUEST")
            .build();
        deleteUser.softDelete();
        userRepository.save(deleteUser);
    }

    @Test
    @DisplayName("아이디 찾기 - 회원 없을 경우")
    void findLogId_user_not_found() throws Exception {
        //given
        String requestJson = """
                {
                    "name": "user1",
                    "email": "test@gmail.com",
                    "phone": "01012345678"
                }
            """;

        //when then
        mockMvc.perform(post("/api/users/find/loginId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value(UserErrorCode.MEMBER_NOT_FOUND.getMessage()));

    }

    @Test
    @DisplayName("아이디 찾기 - 탈퇴한 회원일 경우")
    void findLogId_user_deleted() throws Exception {
        //given
        String requestJson = """
                {
                    "name": "탈퇴유저",
                    "email": "delete1@gmail.com",
                    "phone": "01012345678"
                }
            """;

        //when then
        mockMvc.perform(post("/api/users/find/loginId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value(UserErrorCode.MEMBER_NOT_FOUND.getMessage()));

    }

    @Test
    @DisplayName("아이디 찾기 - 성공")
    void success_findLogId() throws Exception {
        //given
        String requestJson = """
                {
                    "name": "유저1",
                    "email": "user1@gmail.com",
                    "phone": "01012345678"
                }
            """;

        //when then
        mockMvc.perform(post("/api/users/find/loginId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.loginId").value("user1"));

    }

    @Test
    @DisplayName("비밀번호 재발급 - 회원 없을 경우")
    void resetPassword_user_not_found() throws Exception {
        //given
        String requestJson = """
                {
                    "loginId": "user1",
                    "name": "user1",
                    "email": "test@gmail.com",
                    "phone": "01012345678"
                }
            """;

        //when then
        mockMvc.perform(post("/api/users/reset/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value(UserErrorCode.MEMBER_NOT_FOUND.getMessage()));

    }

    @Test
    @DisplayName("비밀번호 재발급 - 탈퇴한 회원일 경우")
    void resetPassword_user_deleted() throws Exception {
        //given
        String requestJson = """
                {
                    "loginId": "delete1",
                    "name": "탈퇴유저",
                    "email": "delete1@gmail.com",
                    "phone": "01012345678"
                }
            """;

        //when then
        mockMvc.perform(post("/api/users/reset/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value(UserErrorCode.MEMBER_NOT_FOUND.getMessage()));

    }

    @Test
    @DisplayName("비밀번호 재발급 - 성공")
    void success_resetPassword() throws Exception {
        //given
        String requestJson = """
                {
                    "loginId": "user1",
                    "name": "유저1",
                    "email": "user1@gmail.com",
                    "phone": "01012345678"
                }
            """;

        //when then
        mockMvc.perform(post("/api/users/reset/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("로그인 - 성공")
    void success_login() throws Exception {
        //given
        String requestJson = """
            {
                "loginId": "user1",
                "password": "user1",
                "role": "GUEST"
            }
            """;

        //when  then
        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("로그인 성공"))
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(cookie().exists("refresh"))
            .andExpect(header().exists("Authorization"));

        assertTrue(refreshTokenRepository.existsByLoginId("user1"));
    }

    @Test
    @DisplayName("로그인 - 탈퇴한 회원")
    void login_deleted_user() throws Exception {
        //given
        String requestJson = """
            {
                "loginId": "delete1",
                "password": "delete1",
                "role": "GUEST"
            }
            """;

        //when  then
        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.codeName").value("USER_NOT_FOUND"))
            .andExpect(jsonPath("$.message").value(UserErrorCode.USER_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("로그인 - 아이디 비밀번호 불일치")
    void login_incorrect_loginId_password() throws Exception {
        //given
        String requestJson = """
            {
                "loginId": "user1",
                "password": "delete1",
                "role": "GUEST"
            }
            """;

        //when  then
        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.codeName").value("INVALID_INFORMATION"))
            .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 올바르지 않습니다."));
    }

    @Test
    @DisplayName("로그인 - 역할 불일치")
    void login_incorrect_role() throws Exception {
        //given
        String requestJson = """
            {
                "loginId": "user1",
                "password": "user1",
                "role": "HOST"
            }
            """;

        //when  then
        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.codeName").value("ROLE_MISMATCH"))
            .andExpect(jsonPath("$.message").value(UserErrorCode.USER_ROLE_MISMATCH.getMessage()));
    }

    @Test
    @DisplayName("토큰 재발급 - 성공")
    void success_reissue() throws Exception {
        //given
        String loginId = "user1";
        String role = "GUEST";

        String refreshToken = jwtUtil.createJwt("refresh", loginId, role,
            TokenExpireTime.REFRESH_TOKEN_EXPIRATION_MILLIS.getValue());
        refreshTokenRepository.save(RefreshToken.builder()
            .loginId(loginId)
            .refresh(refreshToken)
            .expiration(String.valueOf(LocalDateTime.now().plusDays(1)))
            .build());

        Cookie cookie = new Cookie("refresh", refreshToken);

        //when  then
        mockMvc.perform(post("/api/token/reissue").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").exists())
            .andExpect(cookie().exists("refresh"))
            .andExpect(header().exists("Authorization"));
    }

    @Test
    @DisplayName("토큰 재발급 - refresh 토큰 없음")
    void reissue_refresh_token_not_found() throws Exception {
        //when  then
        mockMvc.perform(post("/api/token/reissue"))
            .andExpect(status().isNotFound())
            .andExpect(
                jsonPath("$.message").value(UserErrorCode.REFRESH_TOKEN_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("토큰 재발급 - 카테고리 불일치")
    void reissue_token_category_mismatch() throws Exception {
        //given
        String loginId = "user1";
        String role = "GUEST";

        String refreshToken = jwtUtil.createJwt("access", loginId, role,
            TokenExpireTime.REFRESH_TOKEN_EXPIRATION_MILLIS.getValue());
        refreshTokenRepository.save(RefreshToken.builder()
            .loginId(loginId)
            .refresh(refreshToken)
            .expiration(String.valueOf(LocalDateTime.now().plusDays(1)))
            .build());

        Cookie cookie = new Cookie("refresh", refreshToken);

        //when  then
        mockMvc.perform(post("/api/token/reissue").cookie(cookie))
            .andExpect(status().isNotFound())
            .andExpect(
                jsonPath("$.message").value(UserErrorCode.REFRESH_TOKEN_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("토큰 재발급 - refresh 토큰 만료")
    void reissue_token_expired() throws Exception {
        //given
        String loginId = "user1";
        String role = "GUEST";

        String refreshToken = jwtUtil.createJwt("refresh", loginId, role, 1L);
        Thread.sleep(10);

        refreshTokenRepository.save(RefreshToken.builder()
            .loginId(loginId)
            .refresh(refreshToken)
            .expiration(String.valueOf(LocalDateTime.now().plusDays(1)))
            .build());

        Cookie cookie = new Cookie("refresh", refreshToken);

        //when  then
        mockMvc.perform(post("/api/token/reissue").cookie(cookie))
            .andExpect(status().isUnauthorized())
            .andExpect(
                jsonPath("$.message").value(UserErrorCode.REFRESH_TOKEN_EXPIRED.getMessage()));
    }
}