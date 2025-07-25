package com.beour.wishlist.service;

import com.beour.global.exception.error.errorcode.SpaceErrorCode;
import com.beour.global.exception.error.errorcode.UserErrorCode;
import com.beour.global.exception.error.errorcode.WishListErrorCode;
import com.beour.global.exception.exceptionType.DuplicateLikesException;
import com.beour.global.exception.exceptionType.LikesNotFoundException;
import com.beour.global.exception.exceptionType.SpaceNotFoundException;
import com.beour.global.exception.exceptionType.UserNotFoundException;
import com.beour.review.domain.repository.ReviewRepository;
import com.beour.space.domain.entity.Space;
import com.beour.space.domain.repository.SpaceRepository;
import com.beour.space.guest.dto.SpaceListSpaceResponseDto;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import com.beour.wishlist.dto.WishListPageResponseDto;
import com.beour.wishlist.entity.Like;
import com.beour.wishlist.repository.LikeRepository;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class WishlistService {

    private final LikeRepository likeRepository;
    private final SpaceRepository spaceRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public Like addSpaceToWishList(Long spaceId) {
        User user = findUserFromToken();
        Space space = getSpace(spaceId);

        Like like = likeRepository.findByUserIdAndSpaceId(user.getId(), spaceId);

        if (like == null) {
            Like saveLike = Like.builder()
                .space(space)
                .user(user)
                .build();

            return likeRepository.save(saveLike);
        }

        if (like.isDeleted()) {
            like.resetDelete();
            return like;
        }

        throw new DuplicateLikesException(WishListErrorCode.ALREADY_IN_WISHLIST);
    }

    @Transactional
    public void deleteSpaceFromWishList(Long spaceId) {
        User user = findUserFromToken();
        Space space = getSpace(spaceId);

        Like like = likeRepository.findByUserIdAndSpaceIdAndDeletedAtIsNull(user.getId(),
            space.getId()).orElseThrow(
            () -> new SpaceNotFoundException(SpaceErrorCode.SPACE_NOT_FOUND)
        );

        like.softDelete();
    }

    private Space getSpace(Long spaceId) {
        return spaceRepository.findByIdAndDeletedAtIsNull(spaceId).orElseThrow(
            () -> new SpaceNotFoundException(SpaceErrorCode.SPACE_NOT_FOUND)
        );
    }

    public WishListPageResponseDto getWishlist(Pageable pageable) {
        User user = findUserFromToken();
        Page<Like> whisList = likeRepository.findByUserIdAndDeletedAtIsNull(user.getId(), pageable);

        if (whisList.isEmpty()) {
            throw new LikesNotFoundException(WishListErrorCode.EMPTY_WISHLIST);
        }

        List<SpaceListSpaceResponseDto> spaces = whisList.stream()
            .map(like -> {
                Space space = like.getSpace();
                Long reviewCount = getReviewCountBySpaceId(space.getId());
                return SpaceListSpaceResponseDto.of(space, true, reviewCount);
            })
            .collect(Collectors.toList());

        return new WishListPageResponseDto(spaces, whisList.isLast(), whisList.getTotalPages());
    }

    private long getReviewCountBySpaceId(Long spaceId) {
        return reviewRepository.countBySpaceIdAndDeletedAtIsNull(spaceId);
    }

    private User findUserFromToken() {
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByLoginIdAndDeletedAtIsNull(loginId).orElseThrow(
            () -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND)
        );
    }

}
