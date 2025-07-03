package com.beour.review.guest.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RecentWrittenReviewResponseDto {

    private String spaceNameAndAddress;
    private String reviewerNickName;
    private LocalDateTime reviewCreatedAt;
    private int rating;
    private List<String> images;
    private String reviewContent;

}
