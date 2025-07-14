package com.beour.banner.service;

import com.beour.banner.dto.BannerListForUserResponseDto;
import com.beour.banner.dto.BannerListResponseDto;
import com.beour.banner.dto.CreateBannerRequestDto;
import com.beour.banner.dto.CreateBannerResponseDto;
import com.beour.banner.entity.Banner;
import com.beour.banner.repository.BannerRepository;
import com.beour.global.file.ImageUploader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class BannerService {

    private final BannerRepository bannerRepository;
    private final ImageUploader imageUploader;

    public CreateBannerResponseDto createBanner(CreateBannerRequestDto requestDto, MultipartFile file) throws IOException {
        String imgUrl = imageUploader.upload(file);

        Banner banner = Banner.builder()
            .imgUrl(imgUrl)
            .linkUrl(requestDto.getLinkUrl())
            .title(requestDto.getTitle())
            .isActive(requestDto.getIsActive())
            .displayOrder(requestDto.getDisplayOrder())
            .startDate(requestDto.getStartDate())
            .endDate(requestDto.getEndDate())
            .build();

        Banner savedBanner = bannerRepository.save(banner);
        return CreateBannerResponseDto.builder()
            .bannerId(savedBanner.getId())
            .build();
    }

    public List<BannerListResponseDto> getBannerList(){
        List<Banner> banners = bannerRepository.findAll();

        if(banners.isEmpty()){
            throw new IllegalStateException("조회된 배너가 없습니다.");
        }

        return banners.stream()
            .filter(banner -> !banner.isDeleted())
            .map(BannerListResponseDto::dtoFrom)
            .collect(Collectors.toList());
    }

    public List<BannerListForUserResponseDto> getBannerListForUser(){
        List<Banner> banners = bannerRepository.findValidBanners(LocalDate.now());

        if(banners.isEmpty()){
            throw new IllegalStateException("조회된 배너가 없습니다.");
        }

        return banners.stream()
            .map(BannerListForUserResponseDto::dtoFrom)
            .collect(Collectors.toList());
    }

}
