package com.beour.space.host.service;

import com.beour.global.exception.exceptionType.SpaceNotFoundException;
import com.beour.global.exception.exceptionType.UserNotFoundException;
import com.beour.review.domain.repository.ReviewRepository;
import com.beour.space.domain.entity.*;
import com.beour.space.domain.repository.*;
import com.beour.space.host.dto.*;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpaceService {

    private final SpaceRepository spaceRepository;
    private final DescriptionRepository descriptionRepository;
    private final TagRepository tagRepository;
    private final SpaceImageRepository spaceImageRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final KakaoMapService kakaoMapService;

    @Transactional
    public Long registerSpace(SpaceRegisterRequestDto dto) {
        User host = findUserFromToken();
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
        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            List<Tag> tags = dto.getTags().stream()
                    .map(content -> Tag.builder().space(space).contents(content).build())
                    .toList();
            tagRepository.saveAll(tags);
        }

        // 4. SpaceImages
        if (dto.getImageUrls() != null && !dto.getImageUrls().isEmpty()) {
            List<SpaceImage> images = dto.getImageUrls().stream()
                    .map(url -> SpaceImage.builder().space(space).imageUrl(url).build())
                    .toList();
            spaceImageRepository.saveAll(images);
        }

        return space.getId();
    }

    @Transactional(readOnly = true)
    public SpaceSimpleResponseDto getSimpleSpaceInfo(Long spaceId) {
        Space space = spaceRepository.findByIdAndDeletedAtIsNull(spaceId)
                .orElseThrow(() -> new SpaceNotFoundException("존재하지 않는 공간입니다."));

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

    @Transactional(readOnly = true)
    public SpaceDetailResponseDto getDetailedSpaceInfo(Long spaceId) {
        Space space = spaceRepository.findByIdAndDeletedAtIsNull(spaceId)
                .orElseThrow(() -> new SpaceNotFoundException("존재하지 않는 공간입니다."));

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
                .imageUrls(space.getSpaceImages().stream().map(SpaceImage::getImageUrl).toList())
                .build();
    }

    @Transactional(readOnly = true)
    public List<HostMySpaceListResponseDto> getMySpaces() {
        User host = findUserFromToken();

        List<Space> spaces = spaceRepository.findByHostAndDeletedAtIsNull(host);

        if (spaces.isEmpty()) {
            throw new RuntimeException("등록된 공간이 없습니다.");
        }

        return spaces.stream()
                .map(space -> {
                    long reviewCount = getReviewCountBySpaceId(space.getId());
                    return HostMySpaceListResponseDto.of(
                            space.getId(),
                            extractDongFromAddress(space.getAddress()),
                            space.getMaxCapacity(),
                            space.getAvgRating(),
                            reviewCount,
                            space.getThumbnailUrl()
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateSpace(Long spaceId, SpaceUpdateRequestDto dto) {
        Space space = findSpaceByIdAndCheckOwnership(spaceId);
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
        if (dto.getTags() != null) {
            tagRepository.deleteBySpace(space);
            if (!dto.getTags().isEmpty()) {
                List<Tag> tags = dto.getTags().stream()
                        .map(content -> Tag.builder().space(space).contents(content).build())
                        .toList();
                tagRepository.saveAll(tags);
            }
        }

        // 4. Images 재저장
        if (dto.getImageUrls() != null) {
            spaceImageRepository.deleteBySpace(space);
            if (!dto.getImageUrls().isEmpty()) {
                List<SpaceImage> images = dto.getImageUrls().stream()
                        .map(url -> SpaceImage.builder().space(space).imageUrl(url).build())
                        .toList();
                spaceImageRepository.saveAll(images);
            }
        }
    }

    @Transactional
    public void updateSpaceBasic(Long id, SpaceUpdateRequestDto dto) {
        Space space = findSpaceByIdAndCheckOwnership(id);

        if (dto.getName() != null) space.updateName(dto.getName());
        if (dto.getAddress() != null) {
            double[] latLng = kakaoMapService.getLatitudeAndLongitude(dto.getAddress());
            space.updateAddress(dto.getAddress(), latLng[0], latLng[1]);
        }
        if (dto.getDetailAddress() != null) space.updateDetailAddress(dto.getDetailAddress());
        if (dto.getPricePerHour() != 0) space.updatePricePerHour(dto.getPricePerHour());
        if (dto.getMaxCapacity() != 0) space.updateMaxCapacity(dto.getMaxCapacity());
        if (dto.getSpaceCategory() != null) space.updateSpaceCategory(dto.getSpaceCategory());
        if (dto.getUseCategory() != null) space.updateUseCategory(dto.getUseCategory());
        if (dto.getThumbnailUrl() != null) space.updateThumbnailUrl(dto.getThumbnailUrl());
    }

    @Transactional
    public void updateSpaceDescription(Long id, SpaceUpdateRequestDto dto) {
        Space space = findSpaceByIdAndCheckOwnership(id);
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
    }

    @Transactional
    public void updateTags(Long id, SpaceUpdateRequestDto dto) {
        Space space = findSpaceByIdAndCheckOwnership(id);

        if (dto.getTags() != null) {
            tagRepository.deleteBySpace(space);
            if (!dto.getTags().isEmpty()) {
                List<Tag> newTags = dto.getTags().stream()
                        .map(content -> new Tag(space, content))
                        .toList();
                tagRepository.saveAll(newTags);
            }
        }
    }

    @Transactional
    public void updateSpaceImages(Long id, SpaceUpdateRequestDto dto) {
        Space space = findSpaceByIdAndCheckOwnership(id);

        if (dto.getImageUrls() != null) {
            spaceImageRepository.deleteBySpace(space);
            if (!dto.getImageUrls().isEmpty()) {
                List<SpaceImage> newImages = dto.getImageUrls().stream()
                        .map(url -> new SpaceImage(space, url))
                        .toList();
                spaceImageRepository.saveAll(newImages);
            }
        }
    }

    @Transactional
    public void deleteSpace(Long spaceId) {
        Space space = findSpaceByIdAndCheckOwnership(spaceId);
        space.delete();
    }

    // 예: 서울시 강남구 역삼동 어딘가 123 -> 역삼동
    private String extractDongFromAddress(String address) {
        String[] parts = address.split(" ");
        if (parts.length >= 3) {
            String dong = parts[2];
            // '동'이 포함되어 있으면 그대로 반환, 없으면 구까지만 반환
            return dong.contains("동") ? dong : parts[1];
        }
        return address;
    }

    private long getReviewCountBySpaceId(Long spaceId) {
        return reviewRepository.countBySpaceIdAndDeletedAtIsNull(spaceId);
    }

    private Space findSpaceByIdAndCheckOwnership(Long spaceId) {
        User currentUser = findUserFromToken();
        Space space = spaceRepository.findByIdAndDeletedAtIsNull(spaceId)
                .orElseThrow(() -> new SpaceNotFoundException("존재하지 않는 공간입니다."));

        if (!space.getHost().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("해당 공간에 대한 권한이 없습니다.");
        }

        return space;
    }

    private User findUserFromToken() {
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByLoginIdAndDeletedAtIsNull(loginId)
                .orElseThrow(() -> new UserNotFoundException("해당 유저를 찾을 수 없습니다."));
    }
}
