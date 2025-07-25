package com.beour.wishlist.dto;

import com.beour.space.guest.dto.SpaceListSpaceResponseDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WishListPageResponseDto {
    private List<SpaceListSpaceResponseDto> spaces;
    private boolean isLast;
    private int totalPage;
}
