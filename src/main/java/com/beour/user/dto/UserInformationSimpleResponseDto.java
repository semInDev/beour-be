package com.beour.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class UserInformationSimpleResponseDto {

    private String userName;
    private String userEmail;

}
