package com.beour.space.host.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SpaceSimpleResponseDto {
    private String name;
    private String address;
    private int pricePerHour;
    private List<String> tags;
    private String thumbnailUrl;
}
