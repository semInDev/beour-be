package com.beour.user.dto;

import com.beour.global.validator.annotation.ValidPhoneNum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
public class FindLoginIdRequestDto {

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    private String email;

    @ValidPhoneNum
    private String phone;

    @Builder
    public FindLoginIdRequestDto(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

}
