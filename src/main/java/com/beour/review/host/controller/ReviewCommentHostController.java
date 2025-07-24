package com.beour.review.host.controller;

import com.beour.global.response.ApiResponse;
import com.beour.review.host.dto.ReviewCommentCreateRequestDto;
import com.beour.review.host.dto.ReviewCommentPageResponseDto;
import com.beour.review.host.dto.ReviewCommentResponseDto;
import com.beour.review.host.dto.ReviewCommentUpdateRequestDto;
import com.beour.review.host.dto.ReviewCommentablePageResponseDto;
import com.beour.review.host.dto.ReviewCommentableResponseDto;
import com.beour.review.host.service.ReviewCommentHostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ReviewCommentHostController {

    private final ReviewCommentHostService reviewCommentHostService;

    @GetMapping("/api/users/me/commentable-reviews")
    public ApiResponse<ReviewCommentablePageResponseDto> getCommentableReviews(Pageable pageable) {
        return ApiResponse.ok(reviewCommentHostService.getCommentableReviews(pageable));
    }

    @GetMapping("/api/users/me/review-comments")
    public ApiResponse<ReviewCommentPageResponseDto> getWrittenReviewComments(Pageable pageable) {
        return ApiResponse.ok(reviewCommentHostService.getWrittenReviewComments(pageable));
    }

    @PostMapping("/api/users/me/review-comments")
    public ApiResponse<String> createReviewComment(@RequestBody @Valid ReviewCommentCreateRequestDto requestDto) {
        reviewCommentHostService.createReviewComment(requestDto);
        return ApiResponse.ok("답글이 저장되었습니다.");
    }

    @PatchMapping("/api/users/me/review-comments/{commentId}")
    public ApiResponse<String> updateReviewComment(@PathVariable(value = "commentId") Long commentId,
                                                   @RequestBody @Valid ReviewCommentUpdateRequestDto requestDto) {
        reviewCommentHostService.updateReviewComment(commentId, requestDto);
        return ApiResponse.ok("답글이 수정되었습니다.");
    }

    @DeleteMapping("/api/users/me/review-comments/{commentId}")
    public ApiResponse<String> deleteReviewComment(@PathVariable(value = "commentId") Long commentId) {
        reviewCommentHostService.deleteReviewComment(commentId);
        return ApiResponse.ok("답글이 삭제되었습니다.");
    }
}
