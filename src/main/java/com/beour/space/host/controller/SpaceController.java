package com.beour.space.host.controller;

import com.beour.space.host.dto.SpaceDetailResponseDto;
import com.beour.space.host.dto.SpaceRegisterRequestDto;
import com.beour.space.host.dto.SpaceUpdateRequestDto;
import com.beour.space.host.dto.SpaceSimpleResponseDto;
import com.beour.space.host.service.SpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/spaces")
public class SpaceController {

    private final SpaceService spaceService;

    @PostMapping
    public ResponseEntity<?> registerSpace(@RequestBody SpaceRegisterRequestDto dto) {
        Long id = spaceService.registerSpace(dto);
        return ResponseEntity.ok("공간이 등록되었습니다. ID: " + id);
    }

  
    @GetMapping("/{id}/simple")
    public ResponseEntity<SpaceSimpleResponseDto> getSimpleInfo(@PathVariable Long id) {
        return ResponseEntity.ok(spaceService.getSimpleSpaceInfo(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpaceDetailResponseDto> getDetailInfo(@PathVariable Long id) {
        return ResponseEntity.ok(spaceService.getDetailedSpaceInfo(id));
    }
  
  
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateSpace(@PathVariable Long id,
                                            @RequestBody SpaceUpdateRequestDto dto) {
        spaceService.updateSpace(id, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateSpacePartial(
            @PathVariable Long id,
            @RequestBody SpaceUpdateRequestDto dto) {
        spaceService.updateSpacePartial(id, dto);
        return ResponseEntity.noContent().build();
    }
  
  
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpace(@PathVariable Long id) {
        spaceService.deleteSpace(id);
        return ResponseEntity.noContent().build();
    }
  
}
