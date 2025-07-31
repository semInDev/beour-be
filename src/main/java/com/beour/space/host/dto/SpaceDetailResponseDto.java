package com.beour.space.host.dto;

import com.beour.space.domain.enums.SpaceCategory;
import com.beour.space.domain.enums.UseCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
public class SpaceDetailResponseDto {
    private Long id;
    private String name;
    private String address;
    private String detailAddress;
    private int pricePerHour;
    private int maxCapacity;
    private SpaceCategory spaceCategory;
    private UseCategory useCategory;
    private Double avgRating;
    private String thumbnailUrl;

    // Description
    private String description;
    private String priceGuide;
    private String facilityNotice;
    private String notice;
    private String locationDescription;
    private String refundPolicy;

    private List<String> tags;
    private List<AvailableTimeDto> availableTimes;
    private List<String> imageUrls;

    @Getter
    @AllArgsConstructor
    public static class AvailableTimeDto {
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
    }
}
