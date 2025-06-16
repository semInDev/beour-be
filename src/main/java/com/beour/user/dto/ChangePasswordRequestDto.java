package com.beour.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChangePasswordRequestDto {

    //todo : 비밀번호 유효성 검증 어노테이션 추가
    @NotBlank(message = "새로운 비밀번호를 입력해주세요.")
    private String newPassword;

}
