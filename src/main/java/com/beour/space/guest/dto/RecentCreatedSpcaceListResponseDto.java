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

    private String addressAndName;
    private String thumbnailUrl;
    private String description;
    private LocalDateTime createdAt;

    public RecentCreatedSpcaceListResponseDto dtoFrom(Space space){
        return RecentCreatedSpcaceListResponseDto.builder()
            .addressAndName(space.getAddress().split(" ")[1] + " / " + space.getName())
            .thumbnailUrl(space.getThumbnailUrl())
            .description(space.getDescription().getDescription())
            .createdAt(space.getCreatedAt())
            .build();
    }

}
