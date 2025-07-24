package com.beour.review.guest.controller;

import com.beour.global.response.ApiResponse;
import com.beour.review.guest.dto.RecentWrittenReviewResponseDto;
import com.beour.review.guest.dto.ReviewDetailResponseDto;
import com.beour.review.guest.dto.ReviewForReservationResponseDto;
import com.beour.review.guest.dto.ReviewRequestDto;
import com.beour.review.guest.dto.ReviewUpdateRequestDto;
import com.beour.review.guest.dto.ReviewableReservationPageResponseDto;
import com.beour.review.guest.dto.WrittenReviewPageResponseDto;
import com.beour.review.guest.service.ReviewGuestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class ReviewGuestController {

    private final ReviewGuestService reviewGuestService;

    @GetMapping("/api/users/me/reviewable-reservations")
    public ApiResponse<ReviewableReservationPageResponseDto> getReviewableReservations(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ApiResponse.ok(reviewGuestService.getReviewableReservations(pageable));
    }

    @GetMapping("/api/users/me/reviews")
    public ApiResponse<WrittenReviewPageResponseDto> getWrittenReviews(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ApiResponse.ok(reviewGuestService.getWrittenReviews(pageable));
    }

    // 예약 정보 조회 (리뷰 작성을 위한)
    @GetMapping("/api/reviews/reservations/{reservationId}")
    public ApiResponse<ReviewForReservationResponseDto> getReservationForReview(@PathVariable(value = "reservationId") Long reservationId) {
        return ApiResponse.ok(reviewGuestService.getReservationForReview(reservationId));
    }

    @PostMapping("/api/users/me/reviews")
    public ApiResponse<String> createReview(@RequestPart @Valid ReviewRequestDto requestDto,
                                            @RequestPart(required = false) List<MultipartFile> images) throws IOException {
        reviewGuestService.createReview(requestDto, images);
        return ApiResponse.ok("Review가 저장되었습니다.");
    }

    // 리뷰 상세 조회
    @GetMapping("/api/users/me/reviews/{reviewId}")
    public ApiResponse<ReviewDetailResponseDto> getReviewDetail(@PathVariable(value = "reviewId") Long reviewId) {
        return ApiResponse.ok(reviewGuestService.getReviewDetail(reviewId));
    }

    @PatchMapping("/api/users/me/reviews/{reviewId}")
    public ApiResponse<String> updateReview(@PathVariable(value = "reviewId") Long reviewId,
                                            @RequestPart @Valid ReviewUpdateRequestDto requestDto,
                                            @RequestPart(required = false) List<MultipartFile> images) throws IOException {
        reviewGuestService.updateReview(reviewId, requestDto, images);
        return ApiResponse.ok("Review가 수정되었습니다.");
    }

    @DeleteMapping("/api/users/me/reviews/{reviewId}")
    public ApiResponse<String> deleteReview(@PathVariable(value = "reviewId") Long reviewId) {
        reviewGuestService.deleteReview(reviewId);
        return ApiResponse.ok("Review가 삭제되었습니다.");
    }

    @GetMapping("/api/reviews/new")
    public ApiResponse<List<RecentWrittenReviewResponseDto>> getNewReviews(){
        return ApiResponse.ok(reviewGuestService.getRecentWrittenReviews());
    }
}