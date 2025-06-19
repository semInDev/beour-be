package com.beour.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.*;

import com.beour.global.exception.exceptionType.DuplicateUserInfoException;
import com.beour.user.dto.SignupRequestDto;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class SignupServiceTest {

    @Autowired
    private SignupService signupService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

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

    @DisplayName("닉네임이 중복됨")
    @Test
    public void nickNameDuplicate(){
        //when then
        assertThrows(DuplicateUserInfoException.class,() -> signupService.checkNicknameDuplicate("testNick"));
    }

    @DisplayName("닉네임이 중복되지 않음")
    @Test
    public void nickNameNotDuplicate(){
        //when
        Boolean isDuplicate = signupService.checkNicknameDuplicate("nick");

        //then
        assertThat(isDuplicate).isFalse();
    }

    @DisplayName("중복된 아이디로 회원가입할 시 에러가 난다.")
    @Test
    public void createUserHasDuplicateLoginId(){
        //given
        SignupRequestDto dto = new SignupRequestDto();
        dto.setName("이름");
        dto.setNickname("닉네임");
        dto.setRole("GUEST");
        dto.setEmail("given@gmail.com");
        dto.setLoginId("test");
        dto.setPassword("1234");
        dto.setPhone("01011112222");

        //when then
        List<User> users = userRepository.findAll();
        assertThrows(DuplicateUserInfoException.class, () -> signupService.create(dto));
        assertThat(users).hasSize(1)
            .extracting("name", "loginId")
            .containsExactlyInAnyOrder(
                tuple("testName", "test")
            );
    }

    @DisplayName("중복된 닉네임으로 회원가입할 시 에러가 난다.")
    @Test
    public void createUserHasDuplicateNickname(){
        //given
        SignupRequestDto dto = new SignupRequestDto();
        dto.setName("이름");
        dto.setNickname("testNick");
        dto.setRole("GUEST");
        dto.setEmail("given@gmail.com");
        dto.setLoginId("loginId");
        dto.setPassword("1234");
        dto.setPhone("01011112222");

        //when then
        List<User> users = userRepository.findAll();
        assertThrows(DuplicateUserInfoException.class, () -> signupService.create(dto));
        assertThat(users).hasSize(1)
            .extracting("name", "nickname")
            .containsExactlyInAnyOrder(
                tuple("testName", "testNick")
            );
    }

    @DisplayName("회원가입을 성공한다.")
    @Test
    public void createUser_success(){
        //given
        String password = "successPw";
        SignupRequestDto dto = new SignupRequestDto();
        dto.setName("성공");
        dto.setNickname("success");
        dto.setRole("GUEST");
        dto.setEmail("succ@gmail.com");
        dto.setLoginId("successId");
        dto.setPassword(password);
        dto.setPhone("01011112222");

        //when
        User user = signupService.create(dto);
        User savedUser = userRepository.findByLoginId(dto.getLoginId()).orElse(null);

        // then
        assertNotNull(user);
        assertEquals(dto.getName(), user.getName());
        assertEquals(dto.getNickname(), user.getNickname());
        assertEquals(dto.getRole(), user.getRole());
        assertEquals(dto.getEmail(), user.getEmail());
        assertEquals(dto.getLoginId(), user.getLoginId());
        assertEquals(dto.getPhone(), user.getPhone());
        System.out.println("dto: " + dto.getPassword());
        System.out.println("user: " + user.getPassword());
        System.out.println("saved : " + savedUser.getPassword());
        assertTrue(passwordEncoder.matches(password, user.getPassword()));

        assertNotNull(savedUser);
        assertEquals(dto.getName(), savedUser.getName());
        assertEquals(dto.getNickname(), savedUser.getNickname());
        assertEquals(dto.getRole(), savedUser.getRole());
        assertEquals(dto.getEmail(), savedUser.getEmail());
        assertEquals(dto.getLoginId(), savedUser.getLoginId());
        assertEquals(dto.getPhone(), savedUser.getPhone());

        assertTrue(passwordEncoder.matches(password, savedUser.getPassword()));
    }

}