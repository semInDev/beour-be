package com.beour.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckDuplicateNickNameDto {

  @NotBlank(message = "닉네임을 적어주세요.")
  private String nickname;
}
