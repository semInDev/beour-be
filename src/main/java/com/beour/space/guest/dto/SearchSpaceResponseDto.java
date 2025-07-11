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
public class SearchSpaceResponseDto {

    private Long spaceId;
    private String spaceName;
    private String thumbnailUrl;
    private int price;
    private String address;
    private int maxCapacity;
    private Double average;
    private Long reviewCount;
    private List<String> tags;

    @Builder
    private SearchSpaceResponseDto(Long spaceId, String spaceName, String thumbnailUrl, int price,
        String address, int maxCapacity, Double average, Long reviewCount, List<String> tags) {
        this.spaceId = spaceId;
        this.spaceName = spaceName;
        this.thumbnailUrl = thumbnailUrl;
        this.price = price;
        this.address = address;
        this.maxCapacity = maxCapacity;
        this.average = average;
        this.reviewCount = reviewCount;
        this.tags = tags;
    }

    public static SearchSpaceResponseDto of(Space space, Long reviewCount) {
        List<String> tagList = space.getTags().stream()
            .map(Tag::getContents)
            .collect(Collectors.toList());

        return SearchSpaceResponseDto.builder()
            .spaceId(space.getId())
            .spaceName(space.getName())
            .thumbnailUrl(space.getThumbnailUrl())
            .price(space.getPricePerHour())
            .address(abstractAddress(space.getAddress()))
            .maxCapacity(space.getMaxCapacity())
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
