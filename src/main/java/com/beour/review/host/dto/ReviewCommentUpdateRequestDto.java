package com.beour.review.host.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCommentUpdateRequestDto {

    @NotBlank(message = "답글 내용은 비어 있을 수 없습니다.")
    private String content;
}
