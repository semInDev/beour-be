package com.beour.user.service;

import static org.junit.jupiter.api.Assertions.*;

import com.beour.global.exception.error.errorcode.UserErrorCode;
import com.beour.global.exception.exceptionType.TokenExpiredException;
import com.beour.global.exception.exceptionType.TokenNotFoundException;
import com.beour.global.exception.exceptionType.UserNotFoundException;
import com.beour.global.jwt.JWTUtil;
import com.beour.token.entity.RefreshToken;
import com.beour.token.repository.RefreshTokenRepository;
import com.beour.user.dto.FindLoginIdRequestDto;
import com.beour.user.dto.FindLoginIdResponseDto;
import com.beour.user.dto.ResetPasswordRequestDto;
import com.beour.user.dto.ResetPasswordResponseDto;
import com.beour.user.entity.User;
import com.beour.user.enums.TokenExpireTime;
import com.beour.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class LoginServiceTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LoginService loginService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JWTUtil jwtUtil;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        User user = User.builder()
            .name("유저1")
            .nickname("user1")
            .email("user1@gmail.com")
            .loginId("user1")
            .password("user1")
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

    @AfterEach
    void cleanUp(){
        refreshTokenRepository.deleteAll();
    }
    @Test
    @DisplayName("로그인아이디찾기-일치하는 회원 없음")
    void findLoginId_user_not_found() {
        //given
        FindLoginIdRequestDto requestDto = FindLoginIdRequestDto.builder()
            .name("유저2")
            .email("user2@gmail.com")
            .phone("01012345678")
            .build();

        //when    then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
            () -> loginService.findLoginId(requestDto));
        assertEquals(UserErrorCode.MEMBER_NOT_FOUND.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("로그인아이디찾기-탈퇴한 회원")
    void findLoginId_deleted_user() {
        //given
        FindLoginIdRequestDto requestDto = FindLoginIdRequestDto.builder()
            .name("탈퇴유저")
            .email("delete1@gmail.com")
            .phone("01012345678")
            .build();

        //when    then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
            () -> loginService.findLoginId(requestDto));
        assertEquals(UserErrorCode.MEMBER_NOT_FOUND.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("로그인아이디찾기-성공")
    void success_findLoginId() {
        //given
        FindLoginIdRequestDto requestDto = FindLoginIdRequestDto.builder()
            .name("유저1")
            .email("user1@gmail.com")
            .phone("01012345678")
            .build();

        //when
        FindLoginIdResponseDto result = loginService.findLoginId(requestDto);

        // then
        assertEquals("user1", result.getLoginId());
    }

    @Test
    @DisplayName("비밀번호 리셋-일치하는 회원 없음")
    void resetPassword_user_not_found() {
        //given
        ResetPasswordRequestDto requestDto = ResetPasswordRequestDto.builder()
            .loginId("user1")
            .name("유저2")
            .email("user2@gamil.com")
            .phone("01012345678")
            .build();

        //when    then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
            () -> loginService.resetPassword(requestDto));
        assertEquals(UserErrorCode.MEMBER_NOT_FOUND.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("비밀번호 리셋-성공")
    void success_resetPassword() {
        //given
        ResetPasswordRequestDto requestDto = ResetPasswordRequestDto.builder()
            .loginId("user1")
            .name("유저1")
            .email("user1@gmail.com")
            .phone("01012345678")
            .build();

        //when
        User before = userRepository.findByLoginId(requestDto.getLoginId()).orElse(null);
        ResetPasswordResponseDto result = loginService.resetPassword(requestDto);

        //then
        User after = userRepository.findByLoginId(requestDto.getLoginId()).orElse(null);
        assertNotEquals(before.getPassword(), after.getPassword());
        assertTrue(passwordEncoder.matches(result.getTempPassword(), after.getPassword()));
    }

    @Test
    @DisplayName("토큰 재발급 - 성공")
    void success_reissueRefreshToken() {
        // given
        String loginId = "user1";
        String role = "GUEST";
        String refreshToken = jwtUtil.createJwt("refresh", loginId, role,
            TokenExpireTime.REFRESH_TOKEN_EXPIRATION_MILLIS.getValue());

        refreshTokenRepository.save(RefreshToken.builder()
            .loginId(loginId)
            .refresh(refreshToken)
            .expiration(String.valueOf(LocalDateTime.now().plusDays(1)))
            .build());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("refresh", refreshToken));

        // when
        String[] result = loginService.reissueRefreshToken(request);

        // then
        assertNotNull(result[0]);
        assertTrue(result[0].startsWith("Bearer "));
        assertNotNull(result[1]);
        assertNotSame(refreshToken, result[1]);
        assertTrue(refreshTokenRepository.existsByRefresh(result[1]));
        assertEquals(1, refreshTokenRepository.count());
    }

    @Test
    @DisplayName("토큰 재발급 - refresh token 없을 경우")
    void reissueRefreshToken_refresh_token_not_found() {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest();


        // when  then
        TokenNotFoundException exception = assertThrows(TokenNotFoundException.class, () -> loginService.reissueRefreshToken(request));
        assertEquals(UserErrorCode.REFRESH_TOKEN_NOT_FOUND.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("토큰 재발급 - refresh token이 아닐 경우")
    void reissueRefreshToken_not_refresh_token() {
        // given
        String loginId = "user1";
        String role = "GUEST";
        String refreshToken = jwtUtil.createJwt("access", loginId, role,
            TokenExpireTime.REFRESH_TOKEN_EXPIRATION_MILLIS.getValue());

        refreshTokenRepository.save(RefreshToken.builder()
            .loginId(loginId)
            .refresh(refreshToken)
            .expiration(String.valueOf(LocalDateTime.now().plusDays(1)))
            .build());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("refresh", refreshToken));

        // when  then
        TokenNotFoundException exception = assertThrows(TokenNotFoundException.class, () -> loginService.reissueRefreshToken(request));
        assertEquals(UserErrorCode.REFRESH_TOKEN_NOT_FOUND.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("토큰 재발급 - refresh token 만료")
    void reissueRefreshToken_refresh_token_expired() throws Exception {
        // given
        String loginId = "user1";
        String role = "GUEST";
        String refreshToken = jwtUtil.createJwt("refresh", loginId, role,
            1L);

        refreshTokenRepository.save(RefreshToken.builder()
            .loginId(loginId)
            .refresh(refreshToken)
            .expiration(String.valueOf(LocalDateTime.now().plusDays(1)))
            .build());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("refresh", refreshToken));

        Thread.sleep(10);

        // when  then
        TokenExpiredException exception = assertThrows(TokenExpiredException.class, () -> loginService.reissueRefreshToken(request));
        assertEquals(UserErrorCode.REFRESH_TOKEN_EXPIRED.getMessage(), exception.getMessage());
    }

}