package com.beour.user.dto;

import com.beour.global.validator.annotation.ValidPhoneNum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ResetPasswordRequestDto {

  @NotBlank(message = "아이디는 필수입니다.")
  private String loginId;

  @NotBlank(message = "이름은 필수입니다.")
  private String name;

  @ValidPhoneNum
  private String phone;

  @NotBlank(message = "이메일은 필수입니다.")
  @Email(message = "올바른 이메일 형식이어야 합니다.")
  private String email;

  @Builder
  public ResetPasswordRequestDto(String loginId, String name, String phone, String email){
    this.loginId = loginId;
    this.name = name;
    this.phone = phone;
    this.email = email;
  }

}
