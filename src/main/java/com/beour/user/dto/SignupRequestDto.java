package com.beour.user.dto;

import com.beour.global.validator.annotation.ValidLoginId;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SignupRequestDto {

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 1, max = 10, message = "닉네임은 1자 이상 10자 이하여야 합니다.")
    private String nickname;

    @NotBlank(message = "역할은 필수입니다.")
    @Pattern(regexp = "^(HOST|GUEST)$", message = "역할은 HOST 또는 GUEST만 가능합니다.")
    private String role;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    private String email;

//    @NotBlank(message = "아이디는 필수입니다.")
//    @Size(min = 5, max = 15, message = "아이디는 5자 이상 15자 이하여야 합니다.")
//    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영어와 숫자만 사용 가능합니다.")
    @ValidLoginId
    private String loginId;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 4, max = 20, message = "비밀번호는 4자 이상 20자 이하여야 합니다.")
    private String password;

    @NotBlank(message = "핸드폰번호는 필수입니다.")
    @Pattern(regexp = "^\\d{10,11}$", message = "전화번호는 숫자만 10~11자리로 입력하세요.")
    private String phone;
}
