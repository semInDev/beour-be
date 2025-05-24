package com.beour.space.guest.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class NearbySpaceResponse {
    private Long spaceId;
    private String name;
    private String thumbnailUrl;
    private String address;
    private int maxCapacity;
    private double latitude;
    private double longitude;
    private double avgRating;
    private int pricePerHour;
    private boolean liked;
    private List<String> tags;
}
