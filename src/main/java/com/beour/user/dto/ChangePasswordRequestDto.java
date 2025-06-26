package com.beour.user.dto;

import com.beour.global.validator.annotation.ValidPassword;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChangePasswordRequestDto {

    @ValidPassword
    private String newPassword;

}
