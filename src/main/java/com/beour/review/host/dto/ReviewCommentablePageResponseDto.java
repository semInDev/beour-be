package com.beour.review.host.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ReviewCommentablePageResponseDto {
    private List<ReviewCommentableResponseDto> reviews;
    private boolean last;
    private int totalPage;
}
