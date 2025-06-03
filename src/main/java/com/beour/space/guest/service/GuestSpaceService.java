package com.beour.space.guest.service;

import com.beour.global.exception.exceptionType.InvalidCredentialsException;
import com.beour.global.exception.exceptionType.SpaceNotFoundException;
import com.beour.global.exception.exceptionType.UserNotFoundException;
import com.beour.space.domain.entity.Space;
import com.beour.space.domain.entity.Tag;
import com.beour.space.domain.repository.SpaceRepository;
import com.beour.space.guest.dto.NearbySpaceResponse;
import com.beour.space.guest.dto.RecentCreatedSpcaceListResponseDto;
import com.beour.user.dto.CustomUserDetails;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import com.beour.wishlist.repository.LikeRepository;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
    public List<NearbySpaceResponse> findNearbySpaces(double userLatitude, double userLongitude, double radiusKm, Long userId) {
        double radiusMeters = radiusKm * 1000;

        List<Space> spaces = spaceRepository.findAllWithinDistance(userLatitude, userLongitude, radiusMeters);

        return spaces.stream().map(space -> {
                    List<String> tags = space.getTags().stream()
                            .map(Tag::getContents)
                            .toList();

                    boolean liked = likeRepository.existsByUserIdAndSpaceId(userId, space.getId());

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
    }

    public List<RecentCreatedSpcaceListResponseDto> getRecentCreatedSpace(){
        User user = findUserFromToken();
        List<Space> spaces = spaceRepository.findTop5ByDeletedAtIsNullOrderByCreatedAtDesc();

        if(spaces.isEmpty()){
            throw new SpaceNotFoundException("최근 등록된 공간이 없습니다.");
        }

        return spaces.stream()
            .map(space -> {
                boolean isLiked = likeRepository.existsByUserIdAndSpaceId(user.getId(), space.getId());
                return new RecentCreatedSpcaceListResponseDto().dtoFrom(space, isLiked);
            })
            .collect(Collectors.toList());
    }

    private User findUserFromToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()){
            throw new InvalidCredentialsException("인증된 유저가 없습니다.");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        User user = userRepository.findByLoginId(userDetails.getUsername()).orElseThrow(
            () -> new UserNotFoundException("해당 유저를 찾을 수 없습니다.")
        );

        if(user.isDeleted()){
            throw new UserNotFoundException("해당 유저를 찾을 수 없습니다.");
        }

        return user;
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
