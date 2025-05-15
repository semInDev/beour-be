package com.beour.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckDuplicateLoginIdDto {

    @NotBlank(message = "아이디를 적어주세요.")
    private String loginId;

}
