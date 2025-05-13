package com.beour.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SignupDto {

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    //todo : unique
    @NotBlank(message = "닉네임은 필수입니다.")
    private String nickname;

    //todo : unique
    @NotBlank(message = "이메일은 필수입니다.")
    private String email;

    //todo : unique
    @NotBlank(message = "아이디는 필수입니다.")
    private String loginId;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;

    @NotBlank(message = "핸드폰번호는 필수입니다.")
    private String phone;

}
