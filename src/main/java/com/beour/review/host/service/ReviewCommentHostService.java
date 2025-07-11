package com.beour.review.host.service;

import com.beour.global.exception.error.errorcode.CommentErrorCode;
import com.beour.global.exception.error.errorcode.ReviewErrorCode;
import com.beour.global.exception.error.errorcode.SpaceErrorCode;
import com.beour.global.exception.error.errorcode.UserErrorCode;
import com.beour.global.exception.exceptionType.DuplicateException;
import com.beour.global.exception.exceptionType.ReviewCommentNotFoundException;
import com.beour.global.exception.exceptionType.ReviewNotFoundException;
import com.beour.global.exception.exceptionType.UnauthorityException;
import com.beour.global.exception.exceptionType.UserNotFoundException;
import com.beour.review.domain.entity.Review;
import com.beour.review.domain.entity.ReviewComment;
import com.beour.review.domain.repository.ReviewCommentRepository;
import com.beour.review.domain.repository.ReviewRepository;
import com.beour.review.host.dto.ReviewCommentCreateRequestDto;
import com.beour.review.host.dto.ReviewCommentResponseDto;
import com.beour.review.host.dto.ReviewCommentUpdateRequestDto;
import com.beour.review.host.dto.ReviewCommentableResponseDto;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ReviewCommentHostService {

    private final ReviewRepository reviewRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ReviewCommentableResponseDto> getCommentableReviews() {
        User host = findUserFromToken();

        List<Review> commentableReviews = reviewRepository.findAll()
                .stream()
                .filter(review -> review.getSpace().getHost().getId().equals(host.getId()))
                .filter(review -> review.getDeletedAt() == null)
                .filter(review -> review.getComment() == null)
                .toList();

        return commentableReviews.stream()
                .map(ReviewCommentableResponseDto::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewCommentResponseDto> getWrittenReviewComments() {
        User host = findUserFromToken();

        List<ReviewComment> writtenComments = reviewCommentRepository.findAll()
                .stream()
                .filter(comment -> comment.getUser().getId().equals(host.getId()))
                .filter(comment -> comment.getDeletedAt() == null)
                .collect(Collectors.toList());

        return writtenComments.stream()
                .map(comment -> ReviewCommentResponseDto.of(comment.getReview(), comment))
                .collect(Collectors.toList());
    }

    @Transactional
    public void createReviewComment(ReviewCommentCreateRequestDto requestDto) {
        User host = findUserFromToken();
        Review review = findReviewById(requestDto.getReviewId());

        validateHostOwnership(host, review);

        if (review.getComment() != null) {
            throw new DuplicateException(CommentErrorCode.COMMENT_ALREADY_EXISTS);
        }

        ReviewComment reviewComment = ReviewComment.builder()
                .user(host)
                .review(review)
                .content(requestDto.getContent())
                .build();

        reviewCommentRepository.save(reviewComment);
    }

    @Transactional
    public void updateReviewComment(Long commentId, ReviewCommentUpdateRequestDto requestDto) {
        User host = findUserFromToken();
        ReviewComment reviewComment = findReviewCommentById(commentId);

        if (!reviewComment.getUser().getId().equals(host.getId())) {
            throw new UnauthorityException(CommentErrorCode.UNAUTHORIZED_COMMENT);
        }

        reviewComment.updateContent(requestDto.getContent());
    }

    @Transactional
    public void deleteReviewComment(Long commentId) {
        User host = findUserFromToken();
        ReviewComment reviewComment = findReviewCommentById(commentId);

        if (!reviewComment.getUser().getId().equals(host.getId())) {
            throw new UnauthorityException(CommentErrorCode.UNAUTHORIZED_COMMENT);
        }

        reviewComment.softDelete();
    }

    private User findUserFromToken() {
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByLoginIdAndDeletedAtIsNull(loginId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));
    }

    private Review findReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(ReviewErrorCode.REVIEW_NOT_FOUND));
    }

    private ReviewComment findReviewCommentById(Long commentId) {
        return reviewCommentRepository.findById(commentId)
                .orElseThrow(() -> new ReviewCommentNotFoundException(CommentErrorCode.COMMENT_NOT_FOUND));
    }

    private void validateHostOwnership(User host, Review review) {
        if (!review.getSpace().getHost().getId().equals(host.getId())) {
            throw new UnauthorityException(SpaceErrorCode.NO_PERMISSION);
        }
    }
}
