package com.beour.user.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.beour.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;

//@SpringBootTest
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp(){
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

    @DisplayName("loginId 일치하고 deletedAt이 null이면 true 반환")
    @Test
    void existsByLoginIdAndDeletedAtIsNull(){
        //when
        Boolean isExistLoginId = userRepository.existsByLoginIdAndDeletedAtIsNull("test");

        //then
        assertThat(isExistLoginId).isTrue();
    }

    @DisplayName("loginId 일치하지 않고 deletedAt이 null이면 false 반환")
    @Test
    void notExistsByLoginIdAndDeletedAtIsNull(){
        //when
        Boolean isExistLoginId = userRepository.existsByLoginIdAndDeletedAtIsNull("testtest");

        //then
        assertThat(isExistLoginId).isFalse();
    }

    @DisplayName("nickname 일치하고 deletedAt이 null이면 true 반환")
    @Test
    void existsByNicknameAndDeletedAtIsNull(){
        //when
        Boolean isExistNickname = userRepository.existsByNicknameAndDeletedAtIsNull("testNick");

        //then
        assertThat(isExistNickname).isTrue();
    }

    @DisplayName("nickname 일치하지 않고 deletedAt이 null이면 false 반환")
    @Test
    void notExistsByNicknameAndDeletedAtIsNull(){
        //when
        Boolean isExistNickname = userRepository.existsByNicknameAndDeletedAtIsNull("test");

        //then
        assertThat(isExistNickname).isFalse();
    }
}
