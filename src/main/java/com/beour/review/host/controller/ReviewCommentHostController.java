package com.beour.review.host.controller;

import com.beour.global.response.ApiResponse;
import com.beour.review.host.dto.ReviewCommentResponseDto;
import com.beour.review.host.dto.ReviewCommentableResponseDto;
import com.beour.review.host.service.ReviewCommentHostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ReviewCommentHostController {

    private final ReviewCommentHostService reviewCommentHostService;

    @GetMapping("/api/host/review-comments/commentable")
    public ApiResponse<List<ReviewCommentableResponseDto>> getCommentableReviews() {
        return ApiResponse.ok(reviewCommentHostService.getCommentableReviews());
    }

    @GetMapping("/api/host/review-comments/written")
    public ApiResponse<List<ReviewCommentResponseDto>> getWrittenReviewComments() {
        return ApiResponse.ok(reviewCommentHostService.getWrittenReviewComments());
    }
}
