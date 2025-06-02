package com.beour.banner.service;

import com.beour.banner.dto.CreateBannerRequestDto;
import com.beour.banner.dto.CreateBannerResponseDto;
import com.beour.banner.entity.Banner;
import com.beour.banner.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BannerService {

    private final BannerRepository bannerRepository;

    public CreateBannerResponseDto createBanner(CreateBannerRequestDto requestDto){
        Banner banner = Banner.builder()
            .imgUrl(requestDto.getImgUrl())
            .linkUrl(requestDto.getLinkUrl())
            .title(requestDto.getTitle())
            .isActive(requestDto.isActive())
            .displayOrder(requestDto.getDisplayOrder())
            .startDate(requestDto.getStartDate())
            .endDate(requestDto.getEndDate())
            .build();

        Banner savedBanner = bannerRepository.save(banner);
        CreateBannerResponseDto responseDto = new CreateBannerResponseDto();
        responseDto.setBannerId(savedBanner.getId());

        return responseDto;
    }

}
