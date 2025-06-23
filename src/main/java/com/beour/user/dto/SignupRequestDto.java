package com.beour.user.dto;

import com.beour.global.validator.annotation.ValidLoginId;
import com.beour.global.validator.annotation.ValidNickname;
import com.beour.global.validator.annotation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequestDto {

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @ValidNickname
    private String nickname;

    @NotBlank(message = "역할은 필수입니다.")
    @Pattern(regexp = "^(HOST|GUEST)$", message = "역할은 HOST 또는 GUEST만 가능합니다.")
    private String role;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    private String email;

    @ValidLoginId
    private String loginId;

    @ValidPassword
    private String password;

    @NotBlank(message = "핸드폰번호는 필수입니다.")
    @Pattern(regexp = "^\\d{10,11}$", message = "전화번호는 숫자만 10~11자리로 입력하세요.")
    private String phone;
}
