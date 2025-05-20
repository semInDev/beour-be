package com.beour.space.host.service;

import com.beour.space.host.dto.SpaceRegisterRequestDto;
import com.beour.space.host.entity.*;
import com.beour.space.host.repository.*;
import com.beour.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
}
