package com.beour.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.beour.global.exception.exceptionType.DuplicateUserInfoException;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class SignupServiceTest {

    @Autowired
    private SignupService signupService;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp(){
        userRepository.deleteAll();
        User user = User.builder()
            .name("testName")
            .nickname("testNick")
            .email("test@gmail.com")
            .loginId("test")
            .password("test")
            .phone("01012345678")
            .role("GUEST")
            .build();

        userRepository.save(user);
    }

    @DisplayName("로그인 아이디가 중복됨")
    @Test
    public void logindIdDuplicate(){
        //when then
        assertThrows(DuplicateUserInfoException.class,() -> signupService.checkLoginIdDuplicate("test"));
    }

    @DisplayName("로그인 아이디가 중복되지 않음")
    @Test
    public void loginIdNotDuplicate(){
        //when
        Boolean isDuplicate = signupService.checkLoginIdDuplicate("loginId");

        //then
        assertThat(isDuplicate).isFalse();
    }

}