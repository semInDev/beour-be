package com.beour.space.host.service;

import com.beour.space.host.dto.SpaceDetailResponseDto;
import com.beour.space.host.dto.SpaceRegisterRequestDto;
import com.beour.space.host.dto.SpaceSimpleResponseDto;
import com.beour.space.host.entity.*;
import com.beour.space.host.repository.*;
import com.beour.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpaceService {

    private final SpaceRepository spaceRepository;
    private final DescriptionRepository descriptionRepository;
    private final TagRepository tagRepository;
    private final AvailableTimeRepository availableTimeRepository;
    private final SpaceImageRepository spaceImageRepository;

    private final KakaoMapService kakaoMapService;
    private final UserService userService;

    @Transactional
    public Long registerSpace(SpaceRegisterRequestDto dto) {
        User host = userService.getUserById(dto.getHostId());
        double[] latitudeAndLongitude = kakaoMapService.getLatLng(dto.getAddress());

        // 1. Space
        Space space = Space.builder()
                .host(host)
                .name(dto.getName())
                .spaceCategory(dto.getSpaceCategory())
                .useCategory(dto.getUseCategory())
                .maxCapacity(dto.getMaxCapacity())
                .address(dto.getAddress())
                .detailAddress(dto.getDetailAddress())
                .pricePerHour(dto.getPricePerHour())
                .thumbnailUrl(dto.getThumbnailUrl())
                .latitude(latitudeAndLongitude[0])
                .longitude(latitudeAndLongitude[1])
                .avgRating(0.0)
                .build();
        spaceRepository.save(space);

        // 2. Description
        Description description = Description.builder()
                .space(space)
                .description(dto.getDescription())
                .priceGuide(dto.getPriceGuide())
                .facilityNotice(dto.getFacilityNotice())
                .notice(dto.getNotice())
                .locationDescription(dto.getLocationDescription())
                .refundPolicy(dto.getRefundPolicy())
                .websiteUrl(dto.getWebsiteUrl())
                .build();
        descriptionRepository.save(description);

        // 3. Tags
        List<Tag> tags = dto.getTags().stream()
                .map(content -> Tag.builder().space(space).contents(content).build())
                .toList();
        tagRepository.saveAll(tags);

        // 4. AvailableTimes
        List<AvailableTime> availableTimes = dto.getAvailableTimes().stream()
                .map(at -> AvailableTime.builder()
                        .space(space)
                        .date(at.getDate())
                        .startTime(at.getStartTime())
                        .endTime(at.getEndTime())
                        .build())
                .toList();
        availableTimeRepository.saveAll(availableTimes);

        // 5. SpaceImages
        List<SpaceImage> images = dto.getImageUrls().stream()
                .map(url -> SpaceImage.builder().space(space).imageUrl(url).build())
                .toList();
        spaceImageRepository.saveAll(images);

        return space.getId();
    }

    @Transactional(readOnly = true)
    public SpaceSimpleResponseDto getSimpleSpaceInfo(Long spaceId) {
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공간입니다."));

        List<String> tagContents = space.getTags().stream()
                .map(Tag::getContents)
                .collect(Collectors.toList());

        return new SpaceSimpleResponseDto(
                space.getName(),
                extractDongFromAddress(space.getAddress()),
                space.getPricePerHour(),
                tagContents,
                space.getThumbnailUrl()
        );
    }

    // 예: 서울시 강남구 역삼동 어딘가 123 -> 서울시 강남구 역삼동
    private String extractDongFromAddress(String address) {
        String[] parts = address.split(" ");
        return parts.length >= 3 ? String.join(" ", parts[0], parts[1], parts[2]) : address;
    }

    @Transactional(readOnly = true)
    public SpaceDetailResponseDto getDetailedSpaceInfo(Long spaceId) {
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공간입니다."));

        Description desc = space.getDescription();

        return SpaceDetailResponseDto.builder()
                .id(space.getId())
                .name(space.getName())
                .address(space.getAddress())
                .detailAddress(space.getDetailAddress())
                .pricePerHour(space.getPricePerHour())
                .maxCapacity(space.getMaxCapacity())
                .spaceCategory(space.getSpaceCategory())
                .useCategory(space.getUseCategory())
                .avgRating(space.getAvgRating())
                .description(desc.getDescription())
                .priceGuide(desc.getPriceGuide())
                .facilityNotice(desc.getFacilityNotice())
                .notice(desc.getNotice())
                .locationDescription(desc.getLocationDescription())
                .refundPolicy(desc.getRefundPolicy())
                .websiteUrl(desc.getWebsiteUrl())
                .tags(space.getTags().stream().map(Tag::getContents).toList())
                .availableTimes(space.getAvailableTimes().stream()
                        .map(t -> new SpaceDetailResponseDto.AvailableTimeDto(
                                t.getDate(), t.getStartTime(), t.getEndTime()))
                        .toList())
                .imageUrls(space.getSpaceImages().stream().map(SpaceImage::getImageUrl).toList())
                .build();
    }
  
    @Transactional
    public void deleteSpace(Long spaceId) {
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공간입니다."));
        space.delete();
    }

}
