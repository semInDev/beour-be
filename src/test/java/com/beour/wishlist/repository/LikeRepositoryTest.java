package com.beour.wishlist.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.beour.space.domain.entity.Space;
import com.beour.space.domain.entity.Tag;
import com.beour.space.domain.repository.SpaceRepository;
import com.beour.space.domain.repository.TagRepository;
import com.beour.space.domain.enums.SpaceCategory;
import com.beour.space.domain.enums.UseCategory;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import com.beour.wishlist.entity.Like;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class LikeRepositoryTest {

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
    @DisplayName("true 값 리턴 - existsByUserIdAndSpaceId 함수")
    void true_existsByUserIdAndSpaceId() {
        //given
        Like like = Like.builder()
            .user(guest)
            .space(space1)
            .build();
        likeRepository.save(like);

        //when
        Boolean isExist = likeRepository.existsByUserIdAndSpaceId(guest.getId(), space1.getId());

        //then
        assertTrue(isExist);
    }

    @Test
    @DisplayName("false 값 리턴 - existsByUserIdAndSpaceId 함수")
    void false_existsByUserIdAndSpaceId() {
        //given
        Like like = Like.builder()
            .user(guest)
            .space(space1)
            .build();
        likeRepository.save(like);

        //when
        Boolean isExist = likeRepository.existsByUserIdAndSpaceId(guest.getId(), space2.getId());

        //then
        assertFalse(isExist);
    }

    @Test
    @DisplayName("찜 공간 1개 가져오기 - findByUserIdAndSpaceIdAndDeletedAtIsNull 함수")
    void get_wishlist_findByUserIdAndSpaceIdAndDeletedAtIsNull() {
        //given
        Like like1 = Like.builder()
            .user(guest)
            .space(space1)
            .build();
        likeRepository.save(like1);

        //when
        Like like = likeRepository.findByUserIdAndSpaceIdAndDeletedAtIsNull(guest.getId(),
            space1.getId()).orElse(null);

        //then
        assertEquals(like.getSpace().getId(), space1.getId());
        assertEquals(like.getUser().getId(), guest.getId());
    }

    @Test
    @DisplayName("찜 리스트 가져오기 - findByUserIdAndDeletedAtIsNull 함수")
    void get_wishlist_findByUserIdAndDeletedAtIsNull() {
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
        List<Like> likes = likeRepository.findByUserIdAndDeletedAtIsNull(guest.getId());

        //then
        assertThat(likes)
            .hasSize(2)
            .allSatisfy(like -> {
                assertThat(like.getUser().getId()).isEqualTo(guest.getId());
                assertThat(like.getDeletedAt()).isNull();
            })
            .extracting(like -> like.getSpace().getId())
            .containsExactlyInAnyOrder(space1.getId(), space2.getId());
    }

    @Test
    @DisplayName("찜 리스트 가져오기(페이징) - findByUserIdAndDeletedAtIsNull 함수")
    void get_wishlist_page_findByUserIdAndDeletedAtIsNull() {
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
        Pageable pageable = PageRequest.of(0, 20);

        //when
        Page<Like> likes = likeRepository.findByUserIdAndDeletedAtIsNull(guest.getId(), pageable);

        //then
        assertThat(likes)
            .hasSize(2)
            .allSatisfy(like -> {
                assertThat(like.getUser().getId()).isEqualTo(guest.getId());
                assertThat(like.getDeletedAt()).isNull();
            })
            .extracting(like -> like.getSpace().getId())
            .containsExactlyInAnyOrder(space1.getId(), space2.getId());
    }
}