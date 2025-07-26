package com.beour.space.guest.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.beour.global.jwt.JWTUtil;
import com.beour.space.domain.entity.Space;
import com.beour.space.domain.enums.SpaceCategory;
import com.beour.space.domain.enums.UseCategory;
import com.beour.space.domain.repository.SpaceRepository;
import com.beour.user.repository.UserRepository;
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
import com.beour.user.entity.User;


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class GuestSpaceControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JWTUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SpaceRepository spaceRepository;

    private User guest;
    private User host;
    private Space resultSpace;
    private Space otherSpace1;
    private Space otherSpace2;
    private Space otherSpace3;
    private String guestToken;

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

        guestToken = jwtUtil.createJwt(
            "access",
            guest.getLoginId(),
            "ROLE_" + guest.getRole(),
            1000L * 60 * 30    // 30분
        );

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

        otherSpace3 = Space.builder()
            .host(host)
            .name("공간3")
            .spaceCategory(SpaceCategory.LEATHER)
            .useCategory(UseCategory.ETC)
            .maxCapacity(20)
            .address("안양시 만안구")
            .detailAddress("투썸건물 4층")
            .pricePerHour(40000)
            .thumbnailUrl("https://example.img")
            .latitude(90.0)
            .longitude(180.0)
            .avgRating(0.0)
            .availableTimes(new ArrayList<>())
            .build();
        spaceRepository.save(otherSpace3);
    }

    @AfterEach
    void tearDown() {
        spaceRepository.deleteAll();
        userRepository.deleteAll();
    }


    /**
     * 근처 장소 검색
     * - 성공
     * - 근처 장소 없을경우...?
     */
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


    /**
     * 장소 검색
     * - 성공
     * - 해당 장소 없을 경우
     * - 검색어 입력안했을경우
     */

    /**
     * 장소 검색후 필터링
     * - 성공
     * - 필터링 선택안했을 경우
     * - 주소 필터링
     * - 가격 필터링
     * - 최소인원 필터링
     * - 용도 카테고리 필터링
     * - 유형 카테고리 필터링
     */

    /**
     * 용도별 카테고리 필터링
     * - 성공
     * - 장소 없을 경우
     */

    /**
     * 유형별 카테고리 필터링
     * - 성공
     * - 장소 없을 경우
     */

    /**
     * 최근에 생긴 장소
     * - 성공
     * - 장소 없을 경우
     */

}