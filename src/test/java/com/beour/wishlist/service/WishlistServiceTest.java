package com.beour.wishlist.service;

import static org.junit.jupiter.api.Assertions.*;

import com.beour.global.exception.exceptionType.DuplicateLikesException;
import com.beour.space.domain.entity.Space;
import com.beour.space.domain.repository.SpaceRepository;
import com.beour.space.host.enums.SpaceCategory;
import com.beour.space.host.enums.UseCategory;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import com.beour.wishlist.entity.Like;
import com.beour.wishlist.repository.LikeRepository;
import java.util.Collections;
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
            .build();
        spaceRepository.save(space1);

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
            .build();
        spaceRepository.save(space2);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        likeRepository.deleteAll();
        spaceRepository.deleteAll();
        userRepository.deleteAll();
    }

    /**
     * 찜 목록 조회
     * 비어있을 경우
     * 뭐가 있을 경우
     *
     * 찜 삭제
     * 없는 공간일 경우
     * 이미 삭제된 공간일 경우
     * 68
     */

    @Test
    @DisplayName("찜하기 - 이미 리스트에 존재하는 경우")
    void fail_add_wishlist_duplicate_space(){
        //given
        Like like = Like.builder()
            .user(guest)
            .space(space1)
            .build();
        likeRepository.save(like);

        //when  then
        assertThrows(DuplicateLikesException.class, () -> wishlistService.addSpaceToWishList(space1.getId()));
    }

    @Test
    @DisplayName("찜하기 - 성공")
    void success_add_wishlist(){
        //given
        Like like = Like.builder()
            .user(guest)
            .space(space1)
            .build();

        //when
        Like saved = likeRepository.save(like);

        //then
        assertEquals(saved.getId(), 1);
        assertEquals(saved.getSpace(), space1);
        assertEquals(saved.getUser(), guest);
    }

}