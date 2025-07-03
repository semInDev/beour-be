package com.beour.review.guest.controller;

import com.beour.global.response.ApiResponse;
import com.beour.review.guest.dto.ReviewableReservationResponseDto;
import com.beour.review.guest.dto.WrittenReviewResponseDto;
import com.beour.review.guest.service.ReviewGuestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ReviewGuestController {

    private final ReviewGuestService reviewGuestService;

    @GetMapping("/api/guest/reviews/reviewable")
    public ApiResponse<List<ReviewableReservationResponseDto>> getReviewableReservations() {
        return ApiResponse.ok(reviewGuestService.getReviewableReservations());
    }

    @GetMapping("/api/guest/reviews/written")
    public ApiResponse<List<WrittenReviewResponseDto>> getWrittenReviews() {
        return ApiResponse.ok(reviewGuestService.getWrittenReviews());
    }
}
