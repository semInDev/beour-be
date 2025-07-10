package com.beour.wishlist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.beour.global.exception.exceptionType.DuplicateLikesException;
import com.beour.global.exception.exceptionType.LikesNotFoundException;
import com.beour.space.domain.entity.Space;
import com.beour.space.domain.entity.Tag;
import com.beour.space.domain.repository.SpaceRepository;
import com.beour.space.domain.repository.TagRepository;
import com.beour.space.guest.dto.SpaceListSpaceResponseDto;
import com.beour.space.domain.enums.SpaceCategory;
import com.beour.space.domain.enums.UseCategory;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import com.beour.wishlist.entity.Like;
import com.beour.wishlist.repository.LikeRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
class WishlistServiceTest {

    @Autowired
    private WishlistService wishlistService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private SpaceRepository spaceRepository;
    @Autowired
    private LikeRepository likeRepository;
    @Autowired
    private TagRepository tagRepository;

    private User guest;
    private User host;
    private Space space1;
    private Space space2;


    @BeforeEach
    void setUp() {
        guest = User.builder()
            .loginId("guest")
            .password(passwordEncoder.encode("guestpassword!"))
            .name("게스트")
            .nickname("guest")
            .email("guest@gmail.com")
            .phone("01012345678")
            .role("GUEST")
            .build();
        userRepository.save(guest);

        host = User.builder()
            .loginId("host1")
            .password(passwordEncoder.encode("host1password!"))
            .name("호스트1")
            .nickname("host1")
            .email("host1@gmail.com")
            .phone("01012345678")
            .role("HOST")
            .build();
        userRepository.save(host);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            guest.getLoginId(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        space1 = Space.builder()
            .host(host)
            .name("공간1")
            .spaceCategory(SpaceCategory.COOKING)
            .useCategory(UseCategory.COOKING)
            .maxCapacity(3)
            .address("서울시 강남구")
            .detailAddress("투썸건물 2층")
            .pricePerHour(15000)
            .thumbnailUrl("https://example.img")
            .latitude(123.12)
            .longitude(123.12)
            .avgRating(0.0)
            .tags(new ArrayList<>())
            .build();
        spaceRepository.save(space1);
        Tag tag1 = Tag.builder()
            .space(space1)
            .contents("쿠킹1")
            .build();
        tagRepository.save(tag1);
        space1.getTags().add(tag1);

        space2 = Space.builder()
            .host(host)
            .name("공간2")
            .spaceCategory(SpaceCategory.COOKING)
            .useCategory(UseCategory.COOKING)
            .maxCapacity(3)
            .address("서울시 강남구")
            .detailAddress("투썸건물 2층")
            .pricePerHour(15000)
            .thumbnailUrl("https://example.img")
            .latitude(123.12)
            .longitude(123.12)
            .avgRating(0.0)
            .tags(new ArrayList<>())
            .build();
        spaceRepository.save(space2);
        Tag tag2 = Tag.builder()
            .space(space2)
            .contents("쿠킹2")
            .build();
        tagRepository.save(tag2);
        space2.getTags().add(tag2);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        tagRepository.deleteAll();
        likeRepository.deleteAll();
        spaceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("찜하기 - 이미 리스트에 존재하는 경우")
    void fail_add_wishlist_duplicate_space() {
        //given
        Like like = Like.builder()
            .user(guest)
            .space(space1)
            .build();
        likeRepository.save(like);

        //when  then
        assertThrows(DuplicateLikesException.class,
            () -> wishlistService.addSpaceToWishList(space1.getId()));
    }

    @Test
    @Transactional
    @DisplayName("찜하기 - 같은 공간 찜 삭제 후 다시 찜 등록할 경우")
    void fail_add_wishlist_same_space_again_likes() {
        //given
        Like like = Like.builder()
            .user(guest)
            .space(space1)
            .build();
        likeRepository.save(like);
        like.softDelete();

        //when
        Like saved = wishlistService.addSpaceToWishList(space1.getId());

        //then
        assertEquals(space1.getName(), saved.getSpace().getName());
        assertEquals(null, saved.getDeletedAt());
        assertEquals(saved.getUser(), guest);
    }

    @Test
    @DisplayName("찜하기 - 성공")
    void success_add_wishlist() {
        //when
        Like saved = wishlistService.addSpaceToWishList(space1.getId());

        //then
        assertEquals(space1.getId(), saved.getSpace().getId());
        assertEquals(guest.getId(), saved.getUser().getId());
    }

    @Test
    @DisplayName("찜목록 조회 - 목록이 비어있을 경우")
    void get_wishlist_empty() {
        //when  then
        assertThrows(LikesNotFoundException.class, () -> wishlistService.getWishlist());
    }

    @Test
    @Transactional
    @DisplayName("찜목록 조회 - 성공")
    void success_get_wishlist() {
        //given
        Like like1 = Like.builder()
            .user(guest)
            .space(space1)
            .build();
        likeRepository.save(like1);

        Like like2 = Like.builder()
            .user(guest)
            .space(space2)
            .build();
        likeRepository.save(like2);

        //when
        List<SpaceListSpaceResponseDto> result = wishlistService.getWishlist();

        //then
        assertEquals(result.size(), 2);
        assertEquals(result.get(0).getSpaceName(), "공간1");
        assertEquals(result.get(1).getSpaceName(), "공간2");
    }

    @Test
    @Transactional
    @DisplayName("찜삭제 - 성공")
    void success_delete_wishlist() {
        //given
        Like like1 = Like.builder()
            .user(guest)
            .space(space1)
            .build();
        likeRepository.save(like1);

        Like like2 = Like.builder()
            .user(guest)
            .space(space2)
            .build();
        likeRepository.save(like2);

        //when
        wishlistService.deleteSpaceFromWishList(space2.getId());

        //then
        List<Like> likes = likeRepository.findByUserIdAndDeletedAtIsNull(guest.getId());
        assertThat(likes)
            .hasSize(1)
            .allMatch(like -> !like.getSpace().getId().equals(space2.getId()));

    }

    @Test
    @DisplayName("찜삭제 - 없는 공간일 경우")
    void delete_wishlist_with_empty_list() {
        //when  //then
        assertThrows(IllegalArgumentException.class,
            () -> wishlistService.deleteSpaceFromWishList(space1.getId()));
    }

    @Test
    @Transactional
    @DisplayName("찜삭제 - 이미 삭제한 공간")
    void delete_wishlist_with_deleted_space() {
        //given
        Like like1 = Like.builder()
            .user(guest)
            .space(space1)
            .build();
        likeRepository.save(like1);
        wishlistService.deleteSpaceFromWishList(space1.getId());

        //when  //then
        assertThrows(IllegalArgumentException.class,
            () -> wishlistService.deleteSpaceFromWishList(space1.getId()));
    }
}