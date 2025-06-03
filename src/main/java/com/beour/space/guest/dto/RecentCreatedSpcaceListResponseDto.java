package com.beour.space.guest.dto;

import com.beour.space.domain.entity.Space;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class RecentCreatedSpcaceListResponseDto {

    private String address;
    private String name;
    private int pricePerHour;
    private boolean like;
    private LocalDateTime createdAt;

    public RecentCreatedSpcaceListResponseDto dtoFrom(Space space, boolean like){
        return RecentCreatedSpcaceListResponseDto.builder()
            .address(space.getAddress())
            .name(space.getName())
            .pricePerHour(space.getPricePerHour())
            .like(like)
            .createdAt(space.getCreatedAt())
            .build();
    }

}
