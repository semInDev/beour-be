package com.beour.space.guest.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.beour.global.exception.error.errorcode.SpaceErrorCode;
import com.beour.space.domain.entity.Description;
import com.beour.space.domain.entity.Space;
import com.beour.space.domain.entity.Tag;
import com.beour.space.domain.enums.SpaceCategory;
import com.beour.space.domain.enums.UseCategory;
import com.beour.space.domain.repository.DescriptionRepository;
import com.beour.space.domain.repository.SpaceRepository;
import com.beour.space.domain.repository.TagRepository;
import com.beour.user.repository.UserRepository;
import java.util.ArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.beour.user.entity.User;


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class GuestSpaceControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SpaceRepository spaceRepository;
    @Autowired
    private DescriptionRepository descriptionRepository;
    @Autowired
    private TagRepository tagRepository;

    private User guest;
    private User host;
    private Space resultSpace;
    private Description resultDescription;
    private Tag resultTag;
    private Space otherSpace1;
    private Description description1;
    private Tag tag1;
    private Space otherSpace2;
    private Description description2;
    private Tag tag2;


    @BeforeEach
    void setUp(){
        guest = com.beour.user.entity.User.builder()
            .loginId("guest")
            .password(passwordEncoder.encode("guestpassword!"))
            .name("게스트")
            .nickname("guest")
            .email("guest@gmail.com")
            .phone("01012345678")
            .role("GUEST")
            .build();
        userRepository.save(guest);

        host = com.beour.user.entity.User.builder()
            .loginId("host1")
            .password(passwordEncoder.encode("host1password!"))
            .name("호스트1")
            .nickname("host1")
            .email("host1@gmail.com")
            .phone("01012345678")
            .role("HOST")
            .build();
        userRepository.save(host);

        resultSpace = Space.builder()
            .host(host)
            .name("공간")
            .spaceCategory(SpaceCategory.COOKING)
            .useCategory(UseCategory.COOKING)
            .maxCapacity(5)
            .address("서울시 강남구")
            .detailAddress("투썸건물 1층")
            .pricePerHour(10000)
            .thumbnailUrl("https://example.img")
            .latitude(-90.0)
            .longitude(-150.0)
            .avgRating(0.0)
            .availableTimes(new ArrayList<>())
            .build();
        spaceRepository.save(resultSpace);

        resultDescription = Description.builder()
            .space(resultSpace)
            .description("결과설명")
            .priceGuide("결과가격설명")
            .facilityNotice("결과시설설명")
            .notice("결과공지")
            .locationDescription("결과위치설명")
            .refundPolicy("결과환불설명")
            .build();
        descriptionRepository.save(resultDescription);

        resultTag = Tag.builder()
            .space(resultSpace)
            .contents("동일태그")
            .build();
        tagRepository.save(resultTag);

        otherSpace1 = Space.builder()
            .host(host)
            .name("공간1")
            .spaceCategory(SpaceCategory.ART)
            .useCategory(UseCategory.BARISTA)
            .maxCapacity(1)
            .address("수원시 영통구")
            .detailAddress("투썸건물 2층")
            .pricePerHour(20000)
            .thumbnailUrl("https://example.img")
            .latitude(90.0)
            .longitude(180.0)
            .avgRating(0.0)
            .availableTimes(new ArrayList<>())
            .build();
        spaceRepository.save(otherSpace1);
        description1 = Description.builder()
            .space(otherSpace1)
            .description("설명1")
            .priceGuide("가격설명1")
            .facilityNotice("시설설명1")
            .notice("공지1")
            .locationDescription("위치설명1")
            .refundPolicy("환불설명1")
            .build();
        descriptionRepository.save(description1);
        tag1 = Tag.builder()
            .space(otherSpace1)
            .contents("동일태그")
            .build();
        tagRepository.save(tag1);

        otherSpace2 = Space.builder()
            .host(host)
            .name("공간2")
            .spaceCategory(SpaceCategory.CAFE)
            .useCategory(UseCategory.FILMING)
            .maxCapacity(10)
            .address("수원시 팔달구")
            .detailAddress("투썸건물 3층")
            .pricePerHour(30000)
            .thumbnailUrl("https://example.img")
            .latitude(90.0)
            .longitude(180.0)
            .avgRating(0.0)
            .availableTimes(new ArrayList<>())
            .build();
        spaceRepository.save(otherSpace2);
        description2 = Description.builder()
            .space(otherSpace2)
            .description("설명2")
            .priceGuide("가격설명2")
            .facilityNotice("시설설명2")
            .notice("공지2")
            .locationDescription("위치설명2")
            .refundPolicy("환불설명2")
            .build();
        descriptionRepository.save(description2);
        tag2 = Tag.builder()
            .space(otherSpace2)
            .contents("동일태그")
            .build();
        tagRepository.save(tag2);
    }

    @AfterEach
    void tearDown() {
        descriptionRepository.deleteAll();
        tagRepository.deleteAll();
        spaceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("근처 장소 검색 - 성공")
    void success_nearby() throws Exception {
        //when  then
        mockMvc.perform(get("/api/spaces/nearby")
                .param("latitude", "-90.0")
                .param("longitude", "-150.0")
                .param("radiusKm", "1")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.spaces.length()").value(1))
            .andExpect(jsonPath("$.data.spaces[0].spaceId").value(resultSpace.getId()))
        ;
    }

    @Test
    @DisplayName("근처 장소 검색 - 해당 공간 없을 경우")
    void nearby_space_not_found() throws Exception {
        //when  then
        mockMvc.perform(get("/api/spaces/nearby")
                .param("latitude", "0.0")
                .param("longitude", "0.0")
                .param("radiusKm", "1")
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value(SpaceErrorCode.SPACE_NOT_FOUND.getMessage()))
        ;
    }

    @Test
    @DisplayName("장소 검색 - 성공")
    void success_searchSpaces() throws Exception {
        //when  then
        mockMvc.perform(get("/api/spaces/keyword")
                .param("keyword", "공간")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.spaces.length()").value(3))
        ;
    }

    @Test
    @DisplayName("장소 검색 - 해당 장소 없을 경우")
    void searchSpaces_space_not_found() throws Exception {
        //when  then
        mockMvc.perform(get("/api/spaces/keyword")
                .param("keyword", "에뗇뚭")
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value(SpaceErrorCode.NO_MATCHING_SPACE.getMessage()))
        ;
    }

    @Test
    @DisplayName("장소 검색 - 검색어 입력안했을 경우")
    void searchSpaces_keyword_not_found() throws Exception {
        //when  then
        mockMvc.perform(get("/api/spaces/keyword")
                .param("keyword", "")
            )
            .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("장소 검색 후 필터링 - 성공")
    void success_searchSpacesWithFiltering() throws Exception {
        //given
        String requestJson = String.format("""
            {
                "keyword": "%s",
                "date": null,
                "minPrice": %d,
                "maxPrice": %d,
                "address": "%s",
                "minCapacity": 0,
                "spaceCategories": [],
                "useCategories": []
            }
            """, resultSpace.getName(), resultSpace.getPricePerHour() - 1, resultSpace.getPricePerHour() + 1, resultSpace.getAddress());

        //when  then
        mockMvc.perform(post("/api/spaces/filter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.spaces.length()").value(1))
            .andExpect(jsonPath("$.data.spaces[0].spaceId").value(resultSpace.getId()))
        ;
    }

    @Test
    @DisplayName("장소 검색 후 필터링 - 해당 장소 없는 경우")
    void searchSpacesWithFiltering_space_not_found() throws Exception {
        //given
        String requestJson = String.format("""
            {
                "keyword": "%s",
                "date": null,
                "minPrice": %d,
                "maxPrice": %d,
                "address": "%s",
                "minCapacity": 0,
                "spaceCategories": [],
                "useCategories": []
            }
            """, resultSpace.getName(), resultSpace.getPricePerHour() - 1, resultSpace.getPricePerHour() + 1, otherSpace1.getAddress());

        //when  then
        mockMvc.perform(post("/api/spaces/filter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value(SpaceErrorCode.NO_MATCHING_SPACE.getMessage()))
        ;
    }

    @Test
    @DisplayName("용도별 카테고리 필터링 - 성공")
    void success_searchWithSpaceCategory() throws Exception {
        //when  then
        mockMvc.perform(get("/api/spaces/spacecategory")
                .param("spacecategory", resultSpace.getSpaceCategory().name())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.spaces.length()").value(1))
            .andExpect(jsonPath("$.data.spaces[0].spaceId").value(resultSpace.getId()))
        ;
    }

    @Test
    @DisplayName("용도별 카테고리 필터링 - 해당 장소 없을 경우")
    void searchWithSpaceCategory_space_not_found() throws Exception {
        //when  then
        mockMvc.perform(get("/api/spaces/spacecategory")
                .param("spacecategory", SpaceCategory.COSTUME.name())
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value(SpaceErrorCode.NO_MATCHING_SPACE.getMessage()))
        ;
    }

    @Test
    @DisplayName("유형별 카테고리 필터링 - 성공")
    void success_searchWithUseCategory() throws Exception {
        //when  then
        mockMvc.perform(get("/api/spaces/usecategory")
                .param("usecategory", resultSpace.getUseCategory().name())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.spaces.length()").value(1))
            .andExpect(jsonPath("$.data.spaces[0].spaceId").value(resultSpace.getId()))
        ;
    }

    @Test
    @DisplayName("유형별 카테고리 필터링 - 해당 장소 없을 경우")
    void searchWithUseCategory_space_not_found() throws Exception {
        //when  then
        mockMvc.perform(get("/api/spaces/usecategory")
                .param("usecategory", UseCategory.FLEA_MARKET.name())
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value(SpaceErrorCode.NO_MATCHING_SPACE.getMessage()))
        ;
    }

    @Test
    @DisplayName("최신 공간 불러오기 - 성공")
    void success_getNewSpaces() throws Exception {
        //when  then
        mockMvc.perform(get("/api/spaces/new")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(3))
            .andExpect(jsonPath("$.data[0].spaceId").value(otherSpace2.getId()))
            .andExpect(jsonPath("$.data[1].spaceId").value(otherSpace1.getId()))
            .andExpect(jsonPath("$.data[2].spaceId").value(resultSpace.getId()))
        ;
    }
}