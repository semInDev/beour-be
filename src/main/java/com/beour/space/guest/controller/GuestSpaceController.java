package com.beour.space.guest.controller;

import com.beour.space.guest.dto.NearbySpaceResponse;
import com.beour.space.guest.service.GuestSpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/spaces")
public class GuestSpaceController {
    private final GuestSpaceService guestSpaceService;

    @GetMapping("/nearby")
    public ResponseEntity<List<NearbySpaceResponse>> getNearbySpaces(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam double radiusKm,
            @RequestParam long userId
    ) {
        List<NearbySpaceResponse> response = guestSpaceService.findNearbySpaces(latitude, longitude, radiusKm, userId);
        return ResponseEntity.ok(response);
    }
}
