package com.beour.review.guest.dto;

import com.beour.review.domain.entity.Review;
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
public class ReviewDetailResponseDto {
    private Long reviewId;
    private int rating;
    private String content;
    private LocalDate reservedDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> imageUrls;

    public static ReviewDetailResponseDto of(Review review) {
        List<String> imageUrls = review.getImages().stream()
                .map(image -> image.getImageUrl())
                .collect(Collectors.toList());

        return ReviewDetailResponseDto.builder()
                .reviewId(review.getId())
                .rating(review.getRating())
                .content(review.getContent())
                .reservedDate(review.getReservedDate())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .imageUrls(imageUrls)
                .build();
    }
}
