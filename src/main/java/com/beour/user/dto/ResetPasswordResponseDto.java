package com.beour.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class ResetPasswordResponseDto {

  private String tempPassword;

}
