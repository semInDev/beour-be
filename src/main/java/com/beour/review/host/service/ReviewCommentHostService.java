package com.beour.review.host.service;

import com.beour.global.exception.exceptionType.UserNotFoundException;
import com.beour.review.domain.entity.Review;
import com.beour.review.domain.entity.ReviewComment;
import com.beour.review.domain.repository.ReviewCommentRepository;
import com.beour.review.domain.repository.ReviewRepository;
import com.beour.review.host.dto.ReviewCommentResponseDto;
import com.beour.review.host.dto.ReviewCommentableResponseDto;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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

    private User findUserFromToken() {
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByLoginIdAndDeletedAtIsNull(loginId)
                .orElseThrow(() -> new UserNotFoundException("해당 유저를 찾을 수 없습니다."));
    }
}
