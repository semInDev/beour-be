package com.beour.user.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SignupControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("이름 공란")
    void invalid_name_blank() throws Exception {
        //given
        String requestJson = """
            {
                "name": "",
                "nickname": "testnick",
                "email": "test@gmail.com",
                "loginId": "testid123",
                "password": "test1234!",
                "phone": "01012345678",
                "role": "GUEST"
            }
        """;

        //when then
        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("이름은 필수입니다."));

    }

    @Test
    @DisplayName("닉네임 공란")
    void invalid_nickname_blank() throws Exception {
        //given
        String requestJson = """
            {
                "name": "test",
                "nickname": "",
                "email": "test@gmail.com",
                "loginId": "testid123",
                "password": "test1234!",
                "phone": "01012345678",
                "role": "GUEST"
            }
        """;

        //when then
        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("닉네임은 1자 이상 8자 이하여야 합니다."));
    }

    @Test
    @DisplayName("닉네임 11자리")
    void invalid_nickname_length_eleven() throws Exception {
        //given
        String requestJson = """
            {
                "name": "test",
                "nickname": "eleveneleve",
                "email": "test@gmail.com",
                "loginId": "testid123",
                "password": "test1234!",
                "phone": "01012345678",
                "role": "GUEST"
            }
        """;

        //when then
        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("닉네임은 1자 이상 8자 이하여야 합니다."));
    }

    @Test
    @DisplayName("역할 공란")
    void invalid_role_blank() throws Exception {
        //given
        String requestJson = """
            {
                "name": "test",
                "nickname": "nicktest",
                "email": "test@gmail.com",
                "loginId": "testid123",
                "password": "test1234!",
                "phone": "01012345678",
                "role": ""
            }
        """;

        //when then
        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("역할은 HOST 또는 GUEST만 가능합니다."));
    }

    @Test
    @DisplayName("역할 GUEST, HOST 이외의 값")
    void invalid_role_strangevalue() throws Exception {
        //given
        String requestJson = """
            {
                "name": "test",
                "nickname": "nicktest",
                "email": "test@gmail.com",
                "loginId": "testid123",
                "password": "test1234!",
                "phone": "01012345678",
                "role": "ADMIN"
            }
        """;

        //when then
        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("역할은 HOST 또는 GUEST만 가능합니다."));
    }

    @Test
    @DisplayName("이메일 공란")
    void invalid_email_blank() throws Exception {
        //given
        String requestJson = """
            {
                "name": "test",
                "nickname": "nicktest",
                "email": "",
                "loginId": "testid123",
                "password": "test1234!",
                "phone": "01012345678",
                "role": "GUEST"
            }
        """;

        //when then
        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("이메일은 필수입니다."));
    }

    @Test
    @DisplayName("email 형식 요류")
    void invalid_email_format() throws Exception {
        //given
        String requestJson = """
            {
                "name": "test",
                "nickname": "nicktest",
                "email": "testgmail.com",
                "loginId": "testid123",
                "password": "test1234!",
                "phone": "01012345678",
                "role": "GUEST"
            }
        """;

        //when then
        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value( "올바른 이메일 형식이어야 합니다."));
    }

    @Test
    @DisplayName("아이디 공란")
    void invalid_loginId_blank() throws Exception {
        //given
        String requestJson = """
            {
                "name": "test",
                "nickname": "nicktest",
                "email": "test@gmail.com",
                "loginId": "",
                "password": "test1234!",
                "phone": "01012345678",
                "role": "GUEST"
            }
        """;

        //when then
        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("아이디는 5~15자의 영어 대소문자와 숫자만 사용 가능합니다."));
    }

    @Test
    @DisplayName("아이디 길이가 4")
    void invalid_loginId_length_four() throws Exception {
        //given
        String requestJson = """
            {
                "name": "test",
                "nickname": "nicktest",
                "email": "test@gmail.com",
                "loginId": "test",
                "password": "test1234!",
                "phone": "01012345678",
                "role": "GUEST"
            }
        """;

        //when then
        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("아이디는 5~15자의 영어 대소문자와 숫자만 사용 가능합니다."));
    }

    @Test
    @DisplayName("아이디 길이가 sixteen")
    void invalid_loginId_length_sixteen() throws Exception {
        //given
        String requestJson = """
            {
                "name": "test",
                "nickname": "nicktest",
                "email": "test@gmail.com",
                "loginId": "testtesttesttest",
                "password": "test1234!",
                "phone": "01012345678",
                "role": "GUEST"
            }
        """;

        //when then
        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("아이디는 5~15자의 영어 대소문자와 숫자만 사용 가능합니다."));
    }

    @Test
    @DisplayName("아이디에 특수문자 포함")
    void invalid_loginId_specialcharacter() throws Exception {
        //given
        String requestJson = """
            {
                "name": "test",
                "nickname": "nicktest",
                "email": "test@gmail.com",
                "loginId": "test@@",
                "password": "test1234!",
                "phone": "01012345678",
                "role": "GUEST"
            }
        """;

        //when then
        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("아이디는 5~15자의 영어 대소문자와 숫자만 사용 가능합니다."));
    }

    @Test
    @DisplayName("관리자 아이디일 경우")
    void invalid_loginId_same_admin_loginId() throws Exception {
        //given
        String requestJson = """
            {
                "name": "test",
                "nickname": "nicktest",
                "email": "test@gmail.com",
                "loginId": "admin",
                "password": "test1234!",
                "phone": "01012345678",
                "role": "GUEST"
            }
        """;

        //when then
        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("이미 사용중인 아이디입니다."));
    }

    @Test
    @DisplayName("비밀번호 길이가 7")
    void invalid_password_length_seven() throws Exception {
        //given
        String requestJson = """
            {
                "name": "test",
                "nickname": "nicktest",
                "email": "test@gmail.com",
                "loginId": "testid",
                "password": "test12!",
                "phone": "01012345678",
                "role": "GUEST"
            }
        """;

        //when then
        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("비밀번호는 8자 이상 20자 이하, 특수문자를 1개 이상 포함시켜야합니다."));
    }

    @Test
    @DisplayName("비밀번호 길이가 21")
    void invalid_password_length_twentyone() throws Exception {
        //given
        String requestJson = """
            {
                "name": "test",
                "nickname": "nicktest",
                "email": "test@gmail.com",
                "loginId": "testid",
                "password": "test12!test12!test12!",
                "phone": "01012345678",
                "role": "GUEST"
            }
        """;

        //when then
        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("비밀번호는 8자 이상 20자 이하, 특수문자를 1개 이상 포함시켜야합니다."));
    }

    @Test
    @DisplayName("비밀번호에 특수문자 미포함")
    void invalid_password_without_specialcharacter() throws Exception {
        //given
        String requestJson = """
            {
                "name": "test",
                "nickname": "nicktest",
                "email": "test@gmail.com",
                "loginId": "testid",
                "password": "test1234",
                "phone": "01012345678",
                "role": "GUEST"
            }
        """;

        //when then
        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("비밀번호는 8자 이상 20자 이하, 특수문자를 1개 이상 포함시켜야합니다."));
    }

    @Test
    @DisplayName("핸드폰 번호 길이가 9")
    void invalid_phoneNum_length_nine() throws Exception {
        //given
        String requestJson = """
            {
                "name": "test",
                "nickname": "nicktest",
                "email": "test@gmail.com",
                "loginId": "testid",
                "password": "test1234!",
                "phone": "010123456",
                "role": "GUEST"
            }
        """;

        //when then
        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("전화번호는 숫자만 10~11자리로 입력하세요."));
    }

    @Test
    @DisplayName("핸드폰 번호 길이가 12")
    void invalid_phoneNum_length_twelve() throws Exception {
        //given
        String requestJson = """
            {
                "name": "test",
                "nickname": "nicktest",
                "email": "test@gmail.com",
                "loginId": "testid",
                "password": "test1234!",
                "phone": "010123456789",
                "role": "GUEST"
            }
        """;

        //when then
        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("전화번호는 숫자만 10~11자리로 입력하세요."));
    }

    @Test
    @DisplayName("핸드폰 번호에 숫자말고 다른 문자 포함")
    void invalid_phoneNum() throws Exception {
        //given
        String requestJson = """
            {
                "name": "test",
                "nickname": "nicktest",
                "email": "test@gmail.com",
                "loginId": "testid",
                "password": "test1234!",
                "phone": "010123456r",
                "role": "GUEST"
            }
        """;

        //when then
        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("전화번호는 숫자만 10~11자리로 입력하세요."));
    }
}