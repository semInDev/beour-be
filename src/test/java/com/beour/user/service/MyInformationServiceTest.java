package com.beour.user.service;

import static org.junit.jupiter.api.Assertions.*;

import com.beour.global.exception.exceptionType.InputInvalidFormatException;
import com.beour.user.dto.ChangePasswordRequestDto;
import com.beour.user.dto.UpdateUserInfoRequestDto;
import com.beour.user.dto.UpdateUserInfoResponseDto;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class MyInformationServiceTest {

    @Autowired
    private UserRepository userRepository;
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

    @Test
    @DisplayName("개인정보 수정 - 전부 빈 공간으로 보낼 경우")
    void update_user_info_without_data(){
        //given
        UpdateUserInfoRequestDto requestDto = UpdateUserInfoRequestDto.builder()
            .newNickname("")
            .newPhone("")
            .build();

        //when //then
        assertThrows(InputInvalidFormatException.class, () -> myInformationService.updateUserInfo(requestDto));
    }

    //todo: 중복된 닉네임으로 가입할 경우

    @Test
    @DisplayName("개인정보 수정 - 닉네임만 변경")
    void success_update_user_info_nickname(){
        //given
        String oldPhone = savedUser.getPhone();
        UpdateUserInfoRequestDto requestDto = UpdateUserInfoRequestDto.builder()
            .newNickname("newNick")
            .newPhone("")
            .build();

        //when
        UpdateUserInfoResponseDto result = myInformationService.updateUserInfo(requestDto);

        // then
        assertEquals("newNick", result.getNewNickname());
        assertEquals(oldPhone, result.getNewPhone());
    }

    @Test
    @DisplayName("개인정보 수정 - 핸드폰만 변경")
    void success_update_user_info_phone(){
        //given
        String oldNickname = savedUser.getNickname();
        UpdateUserInfoRequestDto requestDto = UpdateUserInfoRequestDto.builder()
            .newNickname("")
            .newPhone("01011112222")
            .build();

        //when
        UpdateUserInfoResponseDto result = myInformationService.updateUserInfo(requestDto);

        // then
        assertEquals(oldNickname, result.getNewNickname());
        assertEquals("01011112222", result.getNewPhone());
    }

    @Test
    @DisplayName("개인정보 수정 - 모두 변경")
    void success_update_user_all(){
        //given
        UpdateUserInfoRequestDto requestDto = UpdateUserInfoRequestDto.builder()
            .newNickname("newNick")
            .newPhone("01011112222")
            .build();

        //when
        UpdateUserInfoResponseDto result = myInformationService.updateUserInfo(requestDto);

        // then
        assertEquals("newNick", result.getNewNickname());
        assertEquals("01011112222", result.getNewPhone());
    }

    @Test
    @DisplayName("비밀번호 변경 - 성공")
    void success_update_password(){
        //given
        ChangePasswordRequestDto requestDto = ChangePasswordRequestDto.builder()
            .newPassword("newpassword!")
            .build();

        //when
        myInformationService.updatePassword(requestDto);

        //then
        User updateUser = userRepository.findById(savedUser.getId()).orElse(null);
        assertTrue(passwordEncoder.matches("newpassword!", updateUser.getPassword()));
    }
}