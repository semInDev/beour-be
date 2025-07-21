package com.beour.banner.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class CreateBannerResponseDto {

    private Long bannerId;

    @Builder
    private CreateBannerResponseDto(Long bannerId){
        this.bannerId = bannerId;
    }

}
