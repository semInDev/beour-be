package com.beour.space.host.controller;

import com.beour.global.response.ApiResponse;
import com.beour.space.host.dto.HostMySpaceListResponseDto;
import com.beour.space.host.dto.SpaceDetailResponseDto;
import com.beour.space.host.dto.SpaceRegisterRequestDto;
import com.beour.space.host.dto.SpaceUpdateRequestDto;
import com.beour.space.host.dto.SpaceSimpleResponseDto;
import com.beour.space.host.service.SpaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/spaces")
public class SpaceController {

    private final SpaceService spaceService;

    @PostMapping
    public ApiResponse<String> registerSpace(@Valid @RequestBody SpaceRegisterRequestDto dto) {
        Long id = spaceService.registerSpace(dto);
        return ApiResponse.ok("공간이 등록되었습니다. ID: " + id);
    }

    @GetMapping("/{id}/simple")
    public ApiResponse<SpaceSimpleResponseDto> getSimpleInfo(@PathVariable Long id) {
        return ApiResponse.ok(spaceService.getSimpleSpaceInfo(id));
    }

    @GetMapping("/{id}")
    public ApiResponse<SpaceDetailResponseDto> getDetailInfo(@PathVariable Long id) {
        return ApiResponse.ok(spaceService.getDetailedSpaceInfo(id));
    }

    @GetMapping("/my-spaces")
    public ApiResponse<List<HostMySpaceListResponseDto>> getMySpaces() {
        return ApiResponse.ok(spaceService.getMySpaces());
    }

    @PutMapping("/{id}")
    public ApiResponse<String> updateSpace(@PathVariable Long id,
                                           @Valid @RequestBody SpaceUpdateRequestDto dto) {
        spaceService.updateSpace(id, dto);
        return ApiResponse.ok("공간이 성공적으로 수정되었습니다.");
    }

    @PatchMapping("/{id}/basic")
    public ApiResponse<String> updateSpaceBasic(@PathVariable Long id,
                                                @Valid @RequestBody SpaceUpdateRequestDto dto) {
        spaceService.updateSpaceBasic(id, dto);
        return ApiResponse.ok("공간 기본 정보가 성공적으로 수정되었습니다.");
    }

    @PatchMapping("/{id}/description")
    public ApiResponse<String> updateSpaceDescription(@PathVariable Long id,
                                                      @Valid @RequestBody SpaceUpdateRequestDto dto) {
        spaceService.updateSpaceDescription(id, dto);
        return ApiResponse.ok("공간 설명이 성공적으로 수정되었습니다.");
    }

    @PatchMapping("/{id}/tags")
    public ApiResponse<String> updateTags(@PathVariable Long id,
                                          @Valid @RequestBody SpaceUpdateRequestDto dto) {
        spaceService.updateTags(id, dto);
        return ApiResponse.ok("태그가 성공적으로 수정되었습니다.");
    }

    @PatchMapping("/{id}/images")
    public ApiResponse<String> updateSpaceImages(@PathVariable Long id,
                                                 @Valid @RequestBody SpaceUpdateRequestDto dto) {
        spaceService.updateSpaceImages(id, dto);
        return ApiResponse.ok("공간 이미지가 성공적으로 수정되었습니다.");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteSpace(@PathVariable Long id) {
        spaceService.deleteSpace(id);
        return ApiResponse.ok("공간이 성공적으로 삭제되었습니다.");
    }
}
