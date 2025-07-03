package com.beour.review.host.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCommentCreateRequestDto {

    private Long reviewId;
    private String content;
}
