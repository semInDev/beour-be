package com.beour.review.guest.dto;

import com.beour.review.domain.entity.Review;
import com.beour.review.domain.entity.ReviewComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WrittenReviewResponseDto {

    private Long reviewId;
    private String guestNickname;
    private int reviewRating;
    private LocalDateTime reviewCreatedAt;
    private String spaceName;
    private LocalDate reservationDate;
    private List<String> reviewImages;
    private String reviewContent;
    private String reviewCommentHostNickname;
    private LocalDateTime reviewCommentCreatedAt;
    private String reviewCommentContent;

    public static WrittenReviewResponseDto of(Review review, ReviewComment comment) {
        return WrittenReviewResponseDto.builder()
                .reviewId(review.getId())
                .guestNickname(review.getGuest().getNickname())
                .reviewRating(review.getRating())
                .reviewCreatedAt(review.getCreatedAt())
                .spaceName(review.getSpace().getName())
                .reservationDate(review.getReservedDate())
                .reviewImages(review.getImages().stream()
                        .map(reviewImage -> reviewImage.getImageUrl())
                        .collect(Collectors.toList()))
                .reviewContent(review.getContent())
                .reviewCommentHostNickname(comment != null ? comment.getUser().getNickname() : null)
                .reviewCommentCreatedAt(comment != null ? comment.getCreatedAt() : null)
                .reviewCommentContent(comment != null ? comment.getContent() : null)
                .build();
    }
}
