package com.beour.space.guest.service;

import com.beour.global.exception.error.errorcode.SpaceErrorCode;
import com.beour.global.exception.exceptionType.InputInvalidFormatException;
import com.beour.global.exception.exceptionType.SpaceNotFoundException;
import com.beour.review.domain.repository.ReviewRepository;
import com.beour.space.domain.entity.Space;
import com.beour.space.domain.repository.SpaceRepository;
import com.beour.space.guest.dto.FilteringSearchRequestDto;
import com.beour.space.guest.dto.SearchSpaceResponseDto;
import com.beour.space.domain.enums.SpaceCategory;
import com.beour.space.domain.enums.UseCategory;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class GuestSpaceSearchService {

    private final SpaceRepository spaceRepository;
    private final ReviewRepository reviewRepository;

    public List<SearchSpaceResponseDto> search(String keyword) {
        List<Space> spaces = searchWithKeyword(keyword);

        return changeToSearchResponseDtoFrom(spaces);
    }

    private List<Space> searchWithKeyword(String keyword) {
        if (keyword.isEmpty()) {
            throw new InputInvalidFormatException(SpaceErrorCode.KEYWORD_REQUIRED);
        }

        List<Space> result = spaceRepository.searchByKeyword("%" + keyword + "%");
        if (result.isEmpty()) {
            throw new SpaceNotFoundException(SpaceErrorCode.NO_MATCHING_SPACE);
        }

        return result;
    }

    public List<SearchSpaceResponseDto> searchWithFiltering(FilteringSearchRequestDto requestDto) {
        List<Space> spaceListWithKeyword = searchWithKeyword(requestDto.getKeyword());
        List<Space> filtering = filterSpaces(spaceListWithKeyword, requestDto);

        if (filtering.isEmpty()) {
            throw new SpaceNotFoundException(SpaceErrorCode.NO_MATCHING_SPACE);
        }

        return changeToSearchResponseDtoFrom(filtering);
    }

    private List<Space> filterSpaces(List<Space> spaceListWithKeyword,
        FilteringSearchRequestDto requestDto) {
        return spaceListWithKeyword.stream()
            .filter(space -> space.getDeletedAt() == null)

            .filter(space -> space.getPricePerHour() >= requestDto.getMinPrice())
            .filter(space -> requestDto.getMaxPrice() == 0
                || space.getPricePerHour() <= requestDto.getMaxPrice())

            .filter(space -> requestDto.getAddress() == null ||
                space.getAddress() != null && space.getAddress().contains(requestDto.getAddress()))

            .filter(space -> requestDto.getMinCapacity() == 0
                || space.getMaxCapacity() >= requestDto.getMinCapacity())

            .filter(
                space -> requestDto.getSpaceCategories() == null || requestDto.getSpaceCategories()
                    .isEmpty() ||
                    requestDto.getSpaceCategories().contains(space.getSpaceCategory()))

            .filter(space -> requestDto.getUseCategories() == null || requestDto.getUseCategories()
                .isEmpty() ||
                requestDto.getUseCategories().contains(space.getUseCategory()))

            .filter(space -> requestDto.getDate() == null ||
                space.getAvailableTimes().stream()
                    .anyMatch(at -> at.getDeletedAt() == null && requestDto.getDate()
                        .equals(at.getDate())))

            .collect(Collectors.toList());
    }

    public List<SearchSpaceResponseDto> searchSpaceWithSpaceCategory(SpaceCategory request) {
        List<Space> space = spaceRepository.findBySpaceCategory(request);

        if (space.isEmpty()) {
            throw new SpaceNotFoundException(SpaceErrorCode.NO_MATCHING_SPACE);
        }

        return changeToSearchResponseDtoFrom(space);
    }

    public List<SearchSpaceResponseDto> searchSpaceWithUseCategory(UseCategory request) {
        List<Space> space = spaceRepository.findByUseCategory(request);

        if (space.isEmpty()) {
            throw new SpaceNotFoundException(SpaceErrorCode.NO_MATCHING_SPACE);
        }

        return changeToSearchResponseDtoFrom(space);
    }

    private List<SearchSpaceResponseDto> changeToSearchResponseDtoFrom(List<Space> spaces) {
        return spaces.stream()
            .map(space -> SearchSpaceResponseDto.of(space, getReviewCountBySpaceId(space.getId())))
            .toList();
    }

    private long getReviewCountBySpaceId(Long spaceId) {
        return reviewRepository.countBySpaceIdAndDeletedAtIsNull(spaceId);
    }

}
