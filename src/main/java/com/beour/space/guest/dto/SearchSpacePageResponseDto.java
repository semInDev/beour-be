package com.beour.space.guest.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SearchSpacePageResponseDto {

    private List<SearchSpaceResponseDto> spaces;
    private boolean last;
    private int totalPage;
}
