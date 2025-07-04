package com.beour.space.host.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HostMySpaceListResponseDto {
    private Long spaceId;
    private String address; // 동만 (ex. 삼성동, 염창동)
    private int maxCapacity;
    private Double avgRating;
    private long reviewCount;
    private String thumbnailUrl;

    public static HostMySpaceListResponseDto of(Long spaceId, String address, int maxCapacity,
                                                Double avgRating, long reviewCount, String thumbnailUrl) {
        return HostMySpaceListResponseDto.builder()
                .spaceId(spaceId)
                .address(address)
                .maxCapacity(maxCapacity)
                .avgRating(avgRating)
                .reviewCount(reviewCount)
                .thumbnailUrl(thumbnailUrl)
                .build();
    }
}
