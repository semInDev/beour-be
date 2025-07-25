package com.beour.space.guest.controller;

import com.beour.global.response.ApiResponse;
import com.beour.space.guest.dto.FilteringSearchRequestDto;
import com.beour.space.guest.dto.NearbySpaceResponse;
import com.beour.space.guest.dto.RecentCreatedSpcaceListResponseDto;
import com.beour.space.guest.dto.SearchSpacePageResponseDto;
import com.beour.space.guest.service.GuestSpaceSearchService;
import com.beour.space.guest.service.GuestSpaceService;
import com.beour.space.domain.enums.SpaceCategory;
import com.beour.space.domain.enums.UseCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/spaces")
public class GuestSpaceController {

    private final GuestSpaceService guestSpaceService;
    private final GuestSpaceSearchService guestSpaceSearchService;

    @GetMapping("/nearby")
    public ResponseEntity<List<NearbySpaceResponse>> getNearbySpaces(
        @RequestParam double latitude,
        @RequestParam double longitude,
        @RequestParam double radiusKm,
        @RequestParam long userId
    ) {
        List<NearbySpaceResponse> response = guestSpaceService.findNearbySpaces(latitude, longitude,
            radiusKm, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/keyword")
    public ApiResponse<SearchSpacePageResponseDto> searchSpaces(
        @RequestParam(value = "keyword") String request,
        @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(guestSpaceSearchService.search(request, pageable));
    }

    @PostMapping("/filter")
    public ApiResponse<SearchSpacePageResponseDto> searchSpacesWithFiltering(
        @RequestBody FilteringSearchRequestDto requestDto, @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(guestSpaceSearchService.searchWithFiltering(requestDto, pageable));
    }

    @GetMapping("/spacecategory")
    public ApiResponse<SearchSpacePageResponseDto> searchWithSpaceCategory(
        @RequestParam(value = "spacecategory") SpaceCategory request,
        @PageableDefault(size = 20) Pageable pageable) {

        return ApiResponse.ok(guestSpaceSearchService.searchSpaceWithSpaceCategory(request, pageable));
    }

    @GetMapping("/usecategory")
    public ApiResponse<SearchSpacePageResponseDto> searchWithUseCategory(
        @RequestParam(value = "usecategory") UseCategory request, @PageableDefault(size = 20) Pageable pageable) {

        return ApiResponse.ok(guestSpaceSearchService.searchSpaceWithUseCategory(request, pageable));
    }

    @GetMapping("/new")
    public ApiResponse<List<RecentCreatedSpcaceListResponseDto>> getNewSpaces() {

        return ApiResponse.ok(guestSpaceService.getRecentCreatedSpace());
    }

}
