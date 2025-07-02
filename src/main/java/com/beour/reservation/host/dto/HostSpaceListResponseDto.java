package com.beour.reservation.host.dto;

import com.beour.space.domain.entity.Space;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HostSpaceListResponseDto {

    private Long spaceId;
    private String spaceName;

    @Builder
    private HostSpaceListResponseDto(Long spaceId, String spaceName) {
        this.spaceId = spaceId;
        this.spaceName = spaceName;
    }

    public static HostSpaceListResponseDto of(Space space) {
        return HostSpaceListResponseDto.builder()
                .spaceId(space.getId())
                .spaceName(space.getName())
                .build();
    }
}
