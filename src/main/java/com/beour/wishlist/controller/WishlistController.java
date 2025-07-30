package com.beour.wishlist.controller;

import com.beour.global.response.ApiResponse;
import com.beour.space.guest.dto.SpaceListSpaceResponseDto;
import com.beour.wishlist.dto.MakeWishlistResponseDto;
import com.beour.wishlist.dto.WishListPageResponseDto;
import com.beour.wishlist.entity.Like;
import com.beour.wishlist.service.WishlistService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping("/api/spaces/{spaceId}/likes")
    public ApiResponse<MakeWishlistResponseDto> addSpaceToWishlist(@PathVariable(value = "spaceId")Long spaceId){
        Like like = wishlistService.addSpaceToWishList(spaceId);
        MakeWishlistResponseDto dto = new MakeWishlistResponseDto("찜 등록이 완료되었습니다.", like.getId());
        return ApiResponse.ok(dto);
    }

    @DeleteMapping("/api/spaces/{spaceId}/likes")
    public ApiResponse<String> deleteSpaceFromWishlist(@PathVariable(value = "spaceId")Long spaceId){
        wishlistService.deleteSpaceFromWishList(spaceId);
        return ApiResponse.ok("찜 삭제가 되었습니다.");
    }

    @GetMapping("/api/likes")
    public ApiResponse<WishListPageResponseDto> getWishlist(@PageableDefault(size = 10) Pageable pageable){
        return ApiResponse.ok(wishlistService.getWishlist(pageable));
    }
}
