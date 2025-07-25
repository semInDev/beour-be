package com.beour.space.guest.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NearbySpacePageResponseDto {
    private List<NearbySpaceResponse> spaces;
    private boolean last;
    private int totalPage;
}
