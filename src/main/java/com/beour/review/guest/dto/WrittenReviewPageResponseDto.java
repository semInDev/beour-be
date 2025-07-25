package com.beour.review.guest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class WrittenReviewPageResponseDto {
    private List<WrittenReviewResponseDto> reviews;
    private boolean last;
    private int totalPage;
}
