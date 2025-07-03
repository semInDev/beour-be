package com.beour.space.host.dto;

import com.beour.space.domain.entity.Space;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HostSpaceListResponseDto {
    private Long spaceId;
    private String name;
    private String address;
    private int maxCapacity;
    private Double avgRating;

    public static HostSpaceListResponseDto of(Space space) {
        return HostSpaceListResponseDto.builder()
                .spaceId(space.getId())
                .name(space.getName())
                .address(space.getAddress())
                .maxCapacity(space.getMaxCapacity())
                .avgRating(space.getAvgRating())
                .build();
    }
}
