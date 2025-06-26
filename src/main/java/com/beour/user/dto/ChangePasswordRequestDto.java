package com.beour.user.dto;

import com.beour.global.validator.annotation.ValidPassword;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
<<<<<<< test/MyInformationService
@AllArgsConstructor
@Builder
=======
>>>>>>> develop
public class ChangePasswordRequestDto {

    @ValidPassword
    private String newPassword;

}
