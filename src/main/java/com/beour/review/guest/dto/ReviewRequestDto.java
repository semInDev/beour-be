package com.beour.review.guest.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDto {
    @NotNull(message = "reservationId 값은 필수")
    private Long reservationId;

    @NotNull(message = "별점은 필수")
    private int rating;

    @NotNull(message = "리뷰 내용은 필수")
    private String content;
    private List<String> imageUrls;
}
