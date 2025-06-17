package com.beour.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UpdateUserInfoResponseDto {

    private String newNickname;

    private String newPhone;

}
