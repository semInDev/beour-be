package com.beour.review.host.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCommentCreateRequestDto {

    @NotNull(message = "reviewId는 필수입니다.")
    private Long reviewId;

    @NotBlank(message = "답글 내용은 비어 있을 수 없습니다.")
    private String content;
}
