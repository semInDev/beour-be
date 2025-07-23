package com.beour.space.guest.service;

import com.beour.global.exception.error.errorcode.SpaceErrorCode;
import com.beour.global.exception.exceptionType.InputInvalidFormatException;
import com.beour.global.exception.exceptionType.SpaceNotFoundException;
import com.beour.review.domain.repository.ReviewRepository;
import com.beour.space.domain.entity.Space;
import com.beour.space.domain.repository.SpaceRepository;
import com.beour.space.guest.dto.FilteringSearchRequestDto;
import com.beour.space.guest.dto.SearchSpacePageResponseDto;
import com.beour.space.guest.dto.SearchSpaceResponseDto;
import com.beour.space.domain.enums.SpaceCategory;
import com.beour.space.domain.enums.UseCategory;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import com.beour.wishlist.repository.LikeRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class GuestSpaceSearchService {

    private final SpaceRepository spaceRepository;
    private final ReviewRepository reviewRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;

    public SearchSpacePageResponseDto search(String keyword, Pageable pageable) {
        Page<Space> spaces = searchWithKeyword(keyword, pageable);
        List<SearchSpaceResponseDto> spaceResponseDtoList = changeToSearchResponseDtoFrom(spaces);

        return new SearchSpacePageResponseDto(spaceResponseDtoList, spaces.isLast(),
            spaces.getTotalPages());
    }

    private Page<Space> searchWithKeyword(String keyword, Pageable pageable) {
        if (keyword.isEmpty()) {
            throw new InputInvalidFormatException(SpaceErrorCode.KEYWORD_REQUIRED);
        }

        Page<Space> result = spaceRepository.searchByKeyword("%" + keyword + "%", pageable);
        if (result.isEmpty()) {
            throw new SpaceNotFoundException(SpaceErrorCode.NO_MATCHING_SPACE);
        }

        return result;
    }

    public SearchSpacePageResponseDto searchWithFiltering(FilteringSearchRequestDto requestDto,
        Pageable pageable) {
        String keyword = requestDto.getKeyword().isBlank() ? null : requestDto.getKeyword();
        String address = requestDto.getAddress().isBlank() ? null : requestDto.getAddress();
        Integer minPrice = requestDto.getMinPrice() == 0 ? null : requestDto.getMinPrice();
        Integer maxPrice = requestDto.getMaxPrice() == 0 ? null : requestDto.getMaxPrice();
        Integer minCapacity = requestDto.getMinCapacity() == 0 ? null : requestDto.getMinCapacity();
        List<SpaceCategory> spaceCategories =
            requestDto.getSpaceCategories().isEmpty() ? null : requestDto.getSpaceCategories();
        List<UseCategory> useCategories =
            requestDto.getUseCategories().isEmpty() ? null : requestDto.getUseCategories();

        LocalDate date = requestDto.getDate();

        Page<Space> result = spaceRepository.searchWithFiltering(
            keyword, minPrice, maxPrice, address, minCapacity, spaceCategories, useCategories, date,pageable
        );

        if (result.isEmpty()) {
            throw new SpaceNotFoundException(SpaceErrorCode.NO_MATCHING_SPACE);
        }

        List<SearchSpaceResponseDto> spaces = changeToSearchResponseDtoFrom(result);
        return new SearchSpacePageResponseDto(spaces, result.isLast(), result.getTotalPages());
    }

    public SearchSpacePageResponseDto searchSpaceWithSpaceCategory(SpaceCategory request,
        Pageable pageable) {
        Page<Space> spaces = spaceRepository.findBySpaceCategory(request, pageable);

        if (spaces.isEmpty()) {
            throw new SpaceNotFoundException(SpaceErrorCode.NO_MATCHING_SPACE);
        }

        List<SearchSpaceResponseDto> spaceResponseDtoList = changeToSearchResponseDtoFrom(spaces);

        return new SearchSpacePageResponseDto(spaceResponseDtoList, spaces.isLast(),
            spaces.getTotalPages());
    }

    public SearchSpacePageResponseDto searchSpaceWithUseCategory(UseCategory request,
        Pageable pageable) {
        Page<Space> spaces = spaceRepository.findByUseCategory(request, pageable);

        if (spaces.isEmpty()) {
            throw new SpaceNotFoundException(SpaceErrorCode.NO_MATCHING_SPACE);
        }

        List<SearchSpaceResponseDto> spaceResponseDtoList = changeToSearchResponseDtoFrom(spaces);

        return new SearchSpacePageResponseDto(spaceResponseDtoList, spaces.isLast(),
            spaces.getTotalPages());
    }

    private List<SearchSpaceResponseDto> changeToSearchResponseDtoFrom(Page<Space> spaces) {
        User user = findUserFromToken();

        return spaces.stream()
            .map(
                space -> toSearchSpaceResponseDto(space, user))
            .toList();
    }

    private SearchSpaceResponseDto toSearchSpaceResponseDto(Space space, User user) {
        long reviewCount = getReviewCountBySpaceId(space.getId());
        boolean isLiked = false;
        if (user != null) {
            isLiked = likeRepository.existsByUserIdAndSpaceIdAndDeletedAtIsNull(user.getId(),
                space.getId());
        }

        return SearchSpaceResponseDto.oftmp(space, reviewCount, isLiked);
    }

    private long getReviewCountBySpaceId(Long spaceId) {
        return reviewRepository.countBySpaceIdAndDeletedAtIsNull(spaceId);
    }

    private User findUserFromToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String loginId = authentication.getName();
        return userRepository.findByLoginIdAndDeletedAtIsNull(loginId).orElse(null);
    }

}
