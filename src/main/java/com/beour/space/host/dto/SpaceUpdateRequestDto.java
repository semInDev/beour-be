package com.beour.space.host.dto;

import com.beour.space.host.enums.SpaceCategory;
import com.beour.space.host.enums.UseCategory;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
public class SpaceUpdateRequestDto {
    private String name;
    private String address;
    private String detailAddress;
    private int pricePerHour;
    private int maxCapacity;
    private SpaceCategory spaceCategory;
    private UseCategory useCategory;
    private String thumbnailUrl;

    // Description
    private String description;
    private String priceGuide;
    private String facilityNotice;
    private String notice;
    private String locationDescription;
    private String refundPolicy;
    private String websiteUrl;

    private List<String> tags;
    private List<AvailableTimeDto> availableTimes;
    private List<String> imageUrls;

    @Getter
    public static class AvailableTimeDto {
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
    }
}
