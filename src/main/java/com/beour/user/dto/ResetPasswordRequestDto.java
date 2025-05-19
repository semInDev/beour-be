package com.beour.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequestDto {

  @NotBlank(message = "아이디는 필수입니다.")
  private String loginId;

  @NotBlank(message = "이름은 필수입니다.")
  private String name;

  @NotBlank(message = "핸드폰 번호는 필수입니다.")
  private String phone;

  @NotBlank(message = "이메일은 필수입니다.")
  private String email;

}
