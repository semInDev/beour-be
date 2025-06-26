package com.beour.user.service;

import static org.junit.jupiter.api.Assertions.*;

import com.beour.user.dto.UserInformationDetailResponseDto;
import com.beour.user.dto.UserInformationSimpleResponseDto;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class MyInformationServiceTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SignupService signupService;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private MyInformationService myInformationService;

    private User savedUser;

    @BeforeEach
    void setUp(){
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

        // SecurityContext 설정
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            savedUser.getLoginId(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown(){
        SecurityContextHolder.clearContext();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("개인정보 간단 조회 - 성공")
    void success_get_user_information_simple(){
        //when
        UserInformationSimpleResponseDto result = myInformationService.getUserInformationSimple();

        //then
        assertEquals("테스트", result.getUserName());
        assertEquals("test@gmail.com", result.getUserEmail());
    }

    @Test
    @DisplayName("개인정보 세부 조회 - 성공")
    void success_get_user_information_detail(){
        //when
        UserInformationDetailResponseDto result = myInformationService.getUserInformationDetail();

        //then
        assertEquals("테스트", result.getName());
        assertEquals("test@gmail.com", result.getEmail());
        assertEquals("test", result.getNickName());
        assertEquals("01012345678", result.getPhoneNum());
    }


}