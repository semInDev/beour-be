package com.beour.user.service;

import static org.junit.jupiter.api.Assertions.*;

import com.beour.global.exception.exceptionType.UserNotFoundException;
import com.beour.user.dto.FindLoginIdRequestDto;
import com.beour.user.dto.FindLoginIdResponseDto;
import com.beour.user.dto.ResetPasswordRequestDto;
import com.beour.user.dto.ResetPasswordResponseDto;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
        assertEquals("일치하는 회원을 찾을 수 없습니다.", exception.getMessage());
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
        assertEquals("일치하는 회원을 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("로그인아이디찾기-성공")
    void success_findLoginId(){
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
        assertEquals("일치하는 회원을 찾을 수 없습니다.", exception.getMessage());
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

}