package com.beour.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class UserInformationDetailResponseDto {

    private String name;
    private String email;
    private String nickName;
    private String phoneNum;

    @Builder
    public UserInformationDetailResponseDto(String name, String email, String nickName, String phoneNum){
        this.name = name;
        this.email = email;
        this.nickName = nickName;
        this.phoneNum = phoneNum;
    }

}
