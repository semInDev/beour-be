package com.beour.review.host.service;

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

    public List<ReviewCommentableResponseDto> getCommentableReviews() {
        User host = findUserFromToken();

        // 호스트가 소유한 공간에 대한 리뷰 중 댓글이 없는 리뷰 조회
        List<Review> commentableReviews = reviewRepository.findAll()
                .stream()
                .filter(review -> review.getSpace().getHost().getId().equals(host.getId()))
                .filter(review -> review.getDeletedAt() == null)
                .filter(review -> review.getComment() == null)
                .collect(Collectors.toList());

        return commentableReviews.stream()
                .map(ReviewCommentableResponseDto::of)
                .collect(Collectors.toList());
    }

    public List<ReviewCommentResponseDto> getWrittenReviewComments() {
        User host = findUserFromToken();

        // 호스트가 작성한 리뷰 댓글 조회
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

        // 이미 댓글이 있는지 확인
        if (review.getComment() != null) {
            throw new IllegalArgumentException("이미 댓글이 작성된 리뷰입니다.");
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

        // 호스트가 해당 댓글의 작성자인지 확인
        if (!reviewComment.getUser().getId().equals(host.getId())) {
            throw new IllegalArgumentException("댓글 수정 권한이 없습니다.");
        }

        reviewComment.updateContent(requestDto.getContent());
    }

    @Transactional
    public void deleteReviewComment(Long commentId) {
        User host = findUserFromToken();
        ReviewComment reviewComment = findReviewCommentById(commentId);

        // 호스트가 해당 댓글의 작성자인지 확인
        if (!reviewComment.getUser().getId().equals(host.getId())) {
            throw new IllegalArgumentException("댓글 삭제 권한이 없습니다.");
        }

        reviewComment.softDelete();
    }

    private User findUserFromToken() {
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByLoginIdAndDeletedAtIsNull(loginId)
                .orElseThrow(() -> new UserNotFoundException("해당 유저를 찾을 수 없습니다."));
    }

    private Review findReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 리뷰입니다."));
    }

    private ReviewComment findReviewCommentById(Long commentId) {
        return reviewCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 댓글입니다."));
    }

    private void validateHostOwnership(User host, Review review) {
        if (!review.getSpace().getHost().getId().equals(host.getId())) {
            throw new IllegalArgumentException("해당 리뷰의 공간 소유자가 아닙니다.");
        }
    }
}
