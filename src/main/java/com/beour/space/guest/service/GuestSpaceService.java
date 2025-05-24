package com.beour.space.guest.service;

import com.beour.space.domain.entity.Space;
import com.beour.space.domain.entity.Tag;
import com.beour.space.domain.repository.SpaceRepository;
import com.beour.space.guest.dto.NearbySpaceResponse;
import com.beour.wishlist.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GuestSpaceService {
    private final SpaceRepository spaceRepository;
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
