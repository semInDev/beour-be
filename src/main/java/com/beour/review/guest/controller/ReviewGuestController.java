package com.beour.review.guest.controller;

import com.beour.global.response.ApiResponse;
import com.beour.review.guest.dto.RecentWrittenReviewResponseDto;
import com.beour.review.guest.dto.ReviewDetailResponseDto;
import com.beour.review.guest.dto.ReviewForReservationResponseDto;
import com.beour.review.guest.dto.ReviewRequestDto;
import com.beour.review.guest.dto.ReviewUpdateRequestDto;
import com.beour.review.guest.dto.ReviewableReservationResponseDto;
import com.beour.review.guest.dto.WrittenReviewResponseDto;
import com.beour.review.guest.service.ReviewGuestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    // 예약 정보 조회 (리뷰 작성을 위한)
    @GetMapping("/api/guest/reviews/reservation/{reservationId}")
    public ApiResponse<ReviewForReservationResponseDto> getReservationForReview(@PathVariable Long reservationId) {
        return ApiResponse.ok(reviewGuestService.getReservationForReview(reservationId));
    }

    @PostMapping("/api/guest/reviews")
    public ApiResponse<String> createReview(@RequestBody @Valid ReviewRequestDto requestDto) {
        reviewGuestService.createReview(requestDto);
        return ApiResponse.ok("Review가 저장되었습니다.");
    }

    // 리뷰 상세 조회
    @GetMapping("/api/guest/reviews/{reviewId}")
    public ApiResponse<ReviewDetailResponseDto> getReviewDetail(@PathVariable Long reviewId) {
        return ApiResponse.ok(reviewGuestService.getReviewDetail(reviewId));
    }

    @PatchMapping("/api/guest/reviews/{reviewId}")
    public ApiResponse<String> updateReview(@PathVariable Long reviewId,
                                            @RequestBody @Valid ReviewUpdateRequestDto requestDto) {
        reviewGuestService.updateReview(reviewId, requestDto);
        return ApiResponse.ok("Review가 수정되었습니다.");
    }

    @DeleteMapping("/api/guest/reviews/{reviewId}")
    public ApiResponse<String> deleteReview(@PathVariable Long reviewId) {
        reviewGuestService.deleteReview(reviewId);
        return ApiResponse.ok("Review가 삭제되었습니다.");
    }

    @GetMapping("/api/reviews/new")
    public ApiResponse<List<RecentWrittenReviewResponseDto>> getNewReviews(){
        return ApiResponse.ok(reviewGuestService.getRecentWrittenReviews());
    }
}