package com.beour.space.guest.service;

import com.beour.global.exception.error.errorcode.SpaceErrorCode;
import com.beour.global.exception.exceptionType.SpaceNotFoundException;
import com.beour.space.domain.entity.Space;
import com.beour.space.domain.entity.Tag;
import com.beour.space.domain.repository.SpaceRepository;
import com.beour.space.guest.dto.NearbySpacePageResponseDto;
import com.beour.space.guest.dto.NearbySpaceResponse;
import com.beour.space.guest.dto.RecentCreatedSpcaceListResponseDto;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import com.beour.wishlist.repository.LikeRepository;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GuestSpaceService {

    private final SpaceRepository spaceRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;

    @Transactional(readOnly = true)
    public NearbySpacePageResponseDto findNearbySpaces(double userLatitude, double userLongitude,
        double radiusKm, Pageable pageable) {
        User user = findUserFromToken();
        double radiusMeters = radiusKm * 1000;

        Page<Space> spacePage = spaceRepository.findAllWithinDistanceWithPaging(
            userLatitude, userLongitude, radiusMeters, pageable);

        List<NearbySpaceResponse> spaces = spacePage.getContent().stream().map(space -> {
            List<String> tags = space.getTags().stream()
                .map(Tag::getContents)
                .toList();

            boolean liked = false;
            if(user != null){
                liked = likeRepository.existsByUserIdAndSpaceId(user.getId(), space.getId());
            }

            return NearbySpaceResponse.builder()
                .spaceId(space.getId())
                .name(space.getName())
                .thumbnailUrl(space.getThumbnailUrl())
                .address(space.getAddress())
                .maxCapacity(space.getMaxCapacity())
                .latitude(space.getLatitude())
                .longitude(space.getLongitude())
                .avgRating(space.getAvgRating())
                .pricePerHour(space.getPricePerHour())
                .liked(liked)
                .tags(tags)
                .build();
        }).toList();

        return new NearbySpacePageResponseDto(spaces, spacePage.isLast(),
            spacePage.getTotalPages());
    }

    public List<RecentCreatedSpcaceListResponseDto> getRecentCreatedSpace() {
        List<Space> spaces = spaceRepository.findTop5ByDeletedAtIsNullOrderByCreatedAtDesc();

        if (spaces.isEmpty()) {
            throw new SpaceNotFoundException(SpaceErrorCode.NO_RECENT_SPACE);
        }

        return spaces.stream()
            .map(space -> {
                return new RecentCreatedSpcaceListResponseDto().dtoFrom(space);
            })
            .collect(Collectors.toList());
    }

    private User findUserFromToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String loginId = authentication.getName();
        return userRepository.findByLoginIdAndDeletedAtIsNull(loginId).orElse(null);
    }

/*    // 거리 계산 함수 (Haversine)
    private double calculateDistance(
            double userLatitude, double userLongitude,
            double spaceLatitude, double spaceLongitude
    ) {
        double earthRadiusKm = 6371.0;

        double latitudeDifferenceRadians = Math.toRadians(spaceLatitude - userLatitude);
        double longitudeDifferenceRadians = Math.toRadians(spaceLongitude - userLongitude);

        double haversineFormula =
                Math.sin(latitudeDifferenceRadians / 2) * Math.sin(latitudeDifferenceRadians / 2) +
                        Math.cos(Math.toRadians(userLatitude)) * Math.cos(Math.toRadians(spaceLatitude)) *
                                Math.sin(longitudeDifferenceRadians / 2) * Math.sin(longitudeDifferenceRadians / 2);

        double angularDistanceRadians = 2 * Math.atan2(Math.sqrt(haversineFormula), Math.sqrt(1 - haversineFormula));

        return earthRadiusKm * angularDistanceRadians;
    }*/

}
