package com.beour.space.host.dto;

import com.beour.space.host.enums.SpaceCategory;
import com.beour.space.host.enums.UseCategory;
import lombok.Getter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
public class SpaceRegisterRequestDto {
    // 1. Space
    private Long hostId;
    private String name;
    private SpaceCategory spaceCategory;
    private UseCategory useCategory;
    private int maxCapacity;
    private String address;
    private String detailAddress;
    private int pricePerHour;
    private String thumbnailUrl;

    // 2. Description
    private String description;
    private String priceGuide;
    private String facilityNotice;
    private String notice;
    private String locationDescription;
    private String refundPolicy;
    private String websiteUrl;

    // 3. Tags
    private List<String> tags;

    // 4. Available Times
    @Getter
    public static class AvailableTimeDto {
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
    }
    private List<AvailableTimeDto> availableTimes;

    // 5. SpaceImages
    private List<String> imageUrls;
}
