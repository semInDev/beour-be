package com.beour.user.dto;

import com.beour.global.validator.annotation.ValidNullableNickname;
import com.beour.global.validator.annotation.ValidNullablePhoneNumber;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class UpdateUserInfoRequestDto {

    @ValidNullableNickname
    private String newNickname;

    @ValidNullablePhoneNumber
    private String newPhone;

}
