package com.beour.user.service;

import static org.junit.jupiter.api.Assertions.*;

import com.beour.global.exception.exceptionType.UserNotFoundException;
import com.beour.global.response.ApiResponse;
import com.beour.user.dto.FindLoginIdRequestDto;
import com.beour.user.dto.FindLoginIdResponseDto;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class LoginServiceTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LoginService loginService;

    @BeforeEach
    void setUp(){
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

//    @Test
//    @DisplayName("로그인아이디찾기-일치하는 회원 없음")
//    void findLoginId_user_not_found(){
//        //given
//        FindLoginIdRequestDto requestDto = new FindLoginIdRequestDto();
//
//        //when    then
//        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> loginService.findLoginId(requestDto));
//        assertEquals("일치하는 회원을 찾을 수 없습니다.", exception.getMessage());
//    }



}