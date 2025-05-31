package com.beour.wishlist.service;

import com.beour.global.exception.exceptionType.DuplicateLikesException;
import com.beour.global.exception.exceptionType.InvalidCredentialsException;
import com.beour.global.exception.exceptionType.SpaceNotFoundException;
import com.beour.global.exception.exceptionType.UserNotFoundException;
import com.beour.space.domain.entity.Space;
import com.beour.space.domain.repository.SpaceRepository;
import com.beour.user.dto.CustomUserDetails;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import com.beour.wishlist.entity.Like;
import com.beour.wishlist.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class WishlistService {

    private final LikeRepository likeRepository;
    private final SpaceRepository spaceRepository;
    private final UserRepository userRepository;

    public Like addSpaceToWishList(Long spaceId) {
        User user = findUserFromToken();
        Space space = spaceRepository.findById(spaceId).orElseThrow(
            () -> new SpaceNotFoundException("해당 공간은 존재하지 않습니다.")
        );
        if(space.isDeleted()){
            throw new SpaceNotFoundException("해당 공간은 존재하지 않습니다.");
        }

        if(likeRepository.existsByUserIdAndSpaceId(user.getId(), spaceId)){
            throw new DuplicateLikesException("이미 찜 목록에 있습니다.");
        }

        Like like = Like.builder()
            .space(space)
            .user(user)
            .build();

        Like saved = likeRepository.save(like);

        return saved;
    }

    private User findUserFromToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new InvalidCredentialsException("인증된 유저가 없습니다.");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        User user = userRepository.findByLoginId(userDetails.getUsername()).orElseThrow(
            () -> new UserNotFoundException("해당 유저를 찾을 수 없습니다.")
        );

        if (user.isDeleted()) {
            throw new UserNotFoundException("해당 유저를 찾을 수 없습니다.");
        }

        return user;
    }

}
