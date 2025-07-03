package com.beour.space.guest.dto;


import com.beour.space.domain.enums.SpaceCategory;
import com.beour.space.domain.enums.UseCategory;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FilteringSearchRequestDto {

    private String keyword;
    private LocalDate date;
    private int minPrice;
    private int maxPrice;
    private String address;
    private int minCapacity;
    private List<SpaceCategory> spaceCategories;
    private List<UseCategory> useCategories;

}
