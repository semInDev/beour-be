package com.beour.space.guest.dto;

import com.beour.space.domain.entity.Space;
import com.beour.space.domain.entity.Tag;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SpaceListSpaceResponseDto {

    //todo: 대표사진 변경

    private Long spaceId;
    private String spaceName;
    private String region;
    private int maxCapacity;
    private int price;
    private String thumbnailUrl;
    private boolean like;
    private double average;
    private Long reviewCount;
    private List<String> tags;

    @Builder
    private SpaceListSpaceResponseDto(Long spaceId, String spaceName, String region,
        int maxCapacity, int price, String thumbnailUrl, boolean like, double average,
        Long reviewCount, List<String> tags) {
        this.spaceId = spaceId;
        this.spaceName = spaceName;
        this.region = region;
        this.maxCapacity = maxCapacity;
        this.price = price;
        this.thumbnailUrl = thumbnailUrl;
        this.like = like;
        this.average = average;
        this.reviewCount = reviewCount;
        this.tags = tags;
    }

    public static SpaceListSpaceResponseDto of(Space space, boolean like, Long reviewCount) {
        List<String> tagList = space.getTags().stream()
            .map(Tag::getContents)
            .collect(Collectors.toList());

        return SpaceListSpaceResponseDto.builder()
            .spaceId(space.getId())
            .spaceName(space.getName())
            .region(abstractAddress(space.getAddress()))
            .maxCapacity(space.getMaxCapacity())
            .price(space.getPricePerHour())
            .thumbnailUrl(space.getThumbnailUrl())
            .like(like)
            .average(space.getAvgRating())
            .reviewCount(reviewCount)
            .tags(tagList)
            .build();
    }

    private static String abstractAddress(String address) {
        String[] splitAddress = address.split(" ");

        if (splitAddress[0].contains("특별시")) {
            splitAddress[0] = splitAddress[0].replace("특별시", "시");
        } else if (splitAddress[1].contains("광역시")) {
            splitAddress[0] = splitAddress[0].replace("광역시", "시");
        }

        return splitAddress[0] + " " + splitAddress[1];
    }

}
