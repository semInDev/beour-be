package com.beour.space.host.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class HostMySpaceListPageResponseDto {
    private List<HostMySpaceListResponseDto> spaces;
    private boolean last;
    private int totalPage;
}
