package com.beour.wishlist.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.beour.global.exception.error.errorcode.SpaceErrorCode;
import com.beour.global.exception.error.errorcode.WishListErrorCode;
import com.beour.global.jwt.JWTUtil;
import com.beour.space.domain.entity.Space;
import com.beour.space.domain.entity.Tag;
import com.beour.space.domain.repository.SpaceRepository;
import com.beour.space.domain.repository.TagRepository;
import com.beour.space.domain.enums.SpaceCategory;
import com.beour.space.domain.enums.UseCategory;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import com.beour.wishlist.entity.Like;
import com.beour.wishlist.repository.LikeRepository;
import java.util.ArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class WishlistControllerTest {

    @Autowired
    private MockMvc mockMvc;
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
    @Autowired
    private JWTUtil jwtUtil;

    private User guest;
    private User host;
    private Space space1;
    private Space space2;
    private String accessToken;


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

        accessToken = jwtUtil.createJwt(
            "access",
            guest.getLoginId(),
            "ROLE_" + guest.getRole(),
            1000L * 60 * 30    // 30분
        );
    }

    @AfterEach
    void tearDown() {
        tagRepository.deleteAll();
        likeRepository.deleteAll();
        spaceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("찜 등록 - 이미 목록에 존재하는 공간")
    void add_wishlist_with_exist_space() throws Exception {
        //given
        Like like = Like.builder()
            .user(guest)
            .space(space1)
            .build();
        likeRepository.save(like);

        //when  then
        mockMvc.perform(get("/api/spaces/" + space1.getId() + "/likes")
                .header("Authorization", "Bearer " + accessToken)
            )
            .andExpect(status().isConflict())
            .andExpect(
                jsonPath("$.message").value(WishListErrorCode.ALREADY_IN_WISHLIST.getMessage()));
    }

    @Test
    @DisplayName("찜 등록 - 성공")
    void success_add_wishlist() throws Exception {
        //when  then
        mockMvc.perform(get("/api/spaces/" + space1.getId() + "/likes")
                .header("Authorization", "Bearer " + accessToken)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.message").value("찜 등록이 완료되었습니다."));
    }

    @Test
    @DisplayName("찜 삭제 - 목록에 없는 공간일 경우")
    void delete_wish_not_exist_space() throws Exception {
        //when  then
        mockMvc.perform(delete("/api/spaces/" + space1.getId() + "/likes")
                .header("Authorization", "Bearer " + accessToken)
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value(SpaceErrorCode.SPACE_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("찜 삭제 - 성공")
    void success_delete_wish() throws Exception {
        //given
        Like like = Like.builder()
            .user(guest)
            .space(space1)
            .build();
        likeRepository.save(like);

        //when  then
        mockMvc.perform(delete("/api/spaces/" + space1.getId() + "/likes")
                .header("Authorization", "Bearer " + accessToken)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value("찜 삭제가 되었습니다."));
    }

    @Test
    @DisplayName("찜 목록 조회 - 아무것도 없을 경우")
    void get_wishlist_empty() throws Exception {
        //when  then
        mockMvc.perform(get("/api/likes?page=0")
                .header("Authorization", "Bearer " + accessToken)
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value(WishListErrorCode.EMPTY_WISHLIST.getMessage()));
    }

    @Test
    @DisplayName("찜 목록 조회 - 성공")
    void success_get_wishlist() throws Exception {
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

        //when  then
        mockMvc.perform(get("/api/likes?page=0")
                .header("Authorization", "Bearer " + accessToken)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.spaces", hasSize(2)))
            .andExpect(jsonPath("$.data.spaces[0].spaceId").value(space1.getId()))
            .andExpect(jsonPath("$.data.spaces[1].spaceId").value(space2.getId()))
            .andExpect(jsonPath("$.data.last").value(true))
            .andExpect(jsonPath("$.data.totalPage").value(1));
    }
}