package com.beour.review.host.controller;

import com.beour.global.response.ApiResponse;
import com.beour.review.host.dto.ReviewCommentCreateRequestDto;
import com.beour.review.host.dto.ReviewCommentResponseDto;
import com.beour.review.host.dto.ReviewCommentUpdateRequestDto;
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

    @PostMapping("/api/host/review-comments")
    public ApiResponse<String> createReviewComment(@RequestBody ReviewCommentCreateRequestDto requestDto) {
        reviewCommentHostService.createReviewComment(requestDto);
        return ApiResponse.ok("답글이 저장되었습니다.");
    }

    @PatchMapping("/api/host/review-comments/{commentId}")
    public ApiResponse<String> updateReviewComment(@PathVariable Long commentId,
                                                 @RequestBody ReviewCommentUpdateRequestDto requestDto) {
        reviewCommentHostService.updateReviewComment(commentId, requestDto);
        return ApiResponse.ok("답글이 수정되었습니다.");
    }

    @DeleteMapping("/api/host/review-comments/{commentId}")
    public ApiResponse<String> deleteReviewComment(@PathVariable Long commentId) {
        reviewCommentHostService.deleteReviewComment(commentId);
        return ApiResponse.ok("답글이 삭제되었습니다.");
    }
}
