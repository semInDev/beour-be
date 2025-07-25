package com.beour.review.guest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ReviewableReservationPageResponseDto {
    private List<ReviewableReservationResponseDto> reservations;
    private boolean last;
    private int totalPage;
}
