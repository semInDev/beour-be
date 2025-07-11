package com.beour.user.dto;

import com.beour.global.validator.annotation.ValidNullableNickname;
import com.beour.global.validator.annotation.ValidNullablePhoneNumber;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class UpdateUserInfoRequestDto {

    @ValidNullableNickname
    private String newNickname;

    @ValidNullablePhoneNumber
    private String newPhone;

    @Builder
    public UpdateUserInfoRequestDto(String newNickname, String newPhone){
        this.newNickname = newNickname;
        this.newPhone = newPhone;
    }

}
