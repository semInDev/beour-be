package com.beour.space.host.service;

import com.beour.space.host.dto.SpaceDetailResponseDto;
import com.beour.space.host.dto.SpaceRegisterRequestDto;
import com.beour.space.host.dto.SpaceUpdateRequestDto;
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
        double[] latitudeAndLongitude = kakaoMapService.getLatitudeAndLongitude(dto.getAddress());

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
    public void updateSpace(Long spaceId, SpaceUpdateRequestDto dto) {
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공간입니다."));

        double[] latitudeAndLongitude = kakaoMapService.getLatitudeAndLongitude(dto.getAddress());

        // 1. Space 수정
        space.update(
                dto.getName(), dto.getAddress(), dto.getDetailAddress(), dto.getPricePerHour(),
                dto.getMaxCapacity(), dto.getSpaceCategory(), dto.getUseCategory(),
                dto.getThumbnailUrl(), latitudeAndLongitude[0], latitudeAndLongitude[1]
        );

        // 2. Description 수정
        Description desc = space.getDescription();
        desc.update(
                dto.getDescription(), dto.getPriceGuide(), dto.getFacilityNotice(), dto.getNotice(),
                dto.getLocationDescription(), dto.getRefundPolicy(), dto.getWebsiteUrl()
        );

        // 3. Tags 재저장
        tagRepository.deleteAll(space.getTags());
        List<Tag> tags = dto.getTags().stream()
                .map(content -> Tag.builder().space(space).contents(content).build())
                .toList();
        tagRepository.saveAll(tags);

        // 4. AvailableTimes 재저장
        availableTimeRepository.deleteAll(space.getAvailableTimes());
        List<AvailableTime> times = dto.getAvailableTimes().stream()
                .map(t -> AvailableTime.builder()
                        .space(space)
                        .date(t.getDate())
                        .startTime(t.getStartTime())
                        .endTime(t.getEndTime())
                        .build())
                .toList();
        availableTimeRepository.saveAll(times);

        // 5. Images 재저장
        spaceImageRepository.deleteAll(space.getSpaceImages());
        List<SpaceImage> images = dto.getImageUrls().stream()
                .map(url -> SpaceImage.builder().space(space).imageUrl(url).build())
                .toList();
        spaceImageRepository.saveAll(images);
    }

    @Transactional
    public void updateSpacePartial(Long id, SpaceUpdateRequestDto dto) {
        Space space = spaceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공간입니다."));

        // Space
        if (dto.getName() != null) space.updateName(dto.getName());

        if (dto.getAddress() != null) {
            double[] latitudeAndLongitude = kakaoMapService.getLatitudeAndLongitude(dto.getAddress());
            space.updateAddress(dto.getAddress(), latitudeAndLongitude[0], latitudeAndLongitude[1]);
        }

        if (dto.getDetailAddress() != null) space.updateDetailAddress(dto.getDetailAddress());
        if (dto.getPricePerHour() != 0) space.updatePricePerHour(dto.getPricePerHour());
        if (dto.getMaxCapacity() != 0) space.updateMaxCapacity(dto.getMaxCapacity());
        if (dto.getSpaceCategory() != null) space.updateSpaceCategory(dto.getSpaceCategory());
        if (dto.getUseCategory() != null) space.updateUseCategory(dto.getUseCategory());
        if (dto.getThumbnailUrl() != null) space.updateThumbnailUrl(dto.getThumbnailUrl());

        // Description
        Description desc = space.getDescription();
        if (desc != null) {
            if (dto.getDescription() != null) desc.updateDescription(dto.getDescription());
            if (dto.getPriceGuide() != null) desc.updatePriceGuide(dto.getPriceGuide());
            if (dto.getFacilityNotice() != null) desc.updateFacilityNotice(dto.getFacilityNotice());
            if (dto.getNotice() != null) desc.updateNotice(dto.getNotice());
            if (dto.getLocationDescription() != null) desc.updateLocationDescription(dto.getLocationDescription());
            if (dto.getRefundPolicy() != null) desc.updateRefundPolicy(dto.getRefundPolicy());
            if (dto.getWebsiteUrl() != null) desc.updateWebsiteUrl(dto.getWebsiteUrl());
        }


        // Tags
        if (dto.getTags() != null) {
            tagRepository.deleteBySpace(space);
            List<Tag> newTags = dto.getTags().stream()
                    .map(contents -> new Tag(space, contents))
                    .toList();
            tagRepository.saveAll(newTags);
        }

        // AvailableTimes
        if (dto.getAvailableTimes() != null) {
            availableTimeRepository.deleteBySpace(space);
            List<AvailableTime> newTimes = dto.getAvailableTimes().stream()
                    .map(t -> new AvailableTime(space, t.getDate(), t.getStartTime(), t.getEndTime()))
                    .toList();
            availableTimeRepository.saveAll(newTimes);
        }

        // Images
        if (dto.getImageUrls() != null) {
            spaceImageRepository.deleteBySpace(space);
            List<SpaceImage> newImages = dto.getImageUrls().stream()
                    .map(url -> new SpaceImage(space, url))
                    .toList();
            spaceImageRepository.saveAll(newImages);
        }
    }


    @Transactional
    public void deleteSpace(Long spaceId) {
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공간입니다."));
        space.delete();
    }

}
