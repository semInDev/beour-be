package com.beour.review.host.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ReviewCommentPageResponseDto {
    private List<ReviewCommentResponseDto> reviewComments;
    private boolean last;
    private int totalPage;
}
