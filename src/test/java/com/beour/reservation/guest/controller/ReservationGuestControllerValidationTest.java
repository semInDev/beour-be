package com.beour.reservation.guest.controller;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.beour.global.jwt.JWTUtil;
import com.beour.reservation.commons.enums.UsagePurpose;
import com.beour.reservation.commons.repository.ReservationRepository;
import com.beour.reservation.guest.dto.ReservationCreateRequest;
import com.beour.space.domain.entity.AvailableTime;
import com.beour.space.domain.entity.Space;
import com.beour.space.domain.repository.AvailableTimeRepository;
import com.beour.space.domain.repository.SpaceRepository;
import com.beour.space.host.enums.SpaceCategory;
import com.beour.space.host.enums.UseCategory;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ReservationGuestControllerValidationTest {

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
    private Space space;
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

        space = Space.builder()
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
            .availableTimes(new ArrayList<>())
            .build();
        spaceRepository.save(space);

        accessToken = jwtUtil.createJwt(
            "access",
            guest.getLoginId(),
            "ROLE_" + guest.getRole(),
            1000L * 60 * 30    // 30분
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        spaceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("예약 등록 유효성 검사 - 호스트 id 값 공백")
    void invalid_create_reservation_host_id_empty() throws Exception {
        //given
        String requestJson = String.format("""
            {
                "hostId": null,
                "spaceId": %d,
                "date": "%s",
                "startTime": "13:00:00",
                "endTime": "14:00:00",
                "price": 15000,
                "guestCount": 2,
                "usagePurpose": "BARISTA_TRAINING",
                "requestMessage": "요청 사항 테스트"
            }
            """, space.getId(), LocalDate.now().plusDays(1));

        //when  then
        mockMvc.perform(post("/api/spaces/reserve")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("호스트의 id 값 필수"));
    }

    @Test
    @DisplayName("예약 등록 유효성 검사 - 공간 id 값 공백")
    void invalid_create_reservation_space_id_empty() throws Exception {
        //given
        String requestJson = String.format("""
            {
                "hostId": %d,
                "spaceId": null,
                "date": "%s",
                "startTime": "13:00:00",
                "endTime": "14:00:00",
                "price": 15000,
                "guestCount": 2,
                "usagePurpose": "BARISTA_TRAINING",
                "requestMessage": "요청 사항 테스트"
            }
            """, host.getId(), LocalDate.now().plusDays(1));

        //when  then
        mockMvc.perform(post("/api/spaces/reserve")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("예약할 공간 id 값 필수"));
    }

    @Test
    @DisplayName("예약 등록 유효성 검사 - 예약 날짜 데이터 공백")
    void invalid_create_reservation_date_empty() throws Exception {
        //given
        String requestJson = String.format("""
            {
                "hostId": %d,
                "spaceId": %d,
                "date": "",
                "startTime": "13:00:00",
                "endTime": "14:00:00",
                "price": 15000,
                "guestCount": 2,
                "usagePurpose": "BARISTA_TRAINING",
                "requestMessage": "요청 사항 테스트"
            }
            """, space.getId(), host.getId());

        //when  then
        mockMvc.perform(post("/api/spaces/reserve")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("예약일자 필수"));
    }

    @Test
    @DisplayName("예약 등록 유효성 검사 - 이용 시작 시간 데이터 공백")
    void invalid_create_reservation_start_time_empty() throws Exception {
        //given
        String requestJson = String.format("""
            {
                "hostId": %d,
                "spaceId": %d,
                "date": "%s",
                "startTime": null,
                "endTime": "14:00:00",
                "price": 15000,
                "guestCount": 2,
                "usagePurpose": "BARISTA_TRAINING",
                "requestMessage": "요청 사항 테스트"
            }
            """, space.getId(), host.getId(), LocalDate.now().plusDays(1));

        //when  then
        mockMvc.perform(post("/api/spaces/reserve")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("이용 시작 시간 필수"));
    }

    @Test
    @DisplayName("예약 등록 유효성 검사 - 이용 마감 시간 데이터 공백")
    void invalid_create_reservation_end_time_empty() throws Exception {
        //given
        String requestJson = String.format("""
            {
                "hostId": %d,
                "spaceId": %d,
                "date": "%s",
                "startTime": "13:00:00",
                "endTime": null,
                "price": 15000,
                "guestCount": 2,
                "usagePurpose": "BARISTA_TRAINING",
                "requestMessage": "요청 사항 테스트"
            }
            """, space.getId(), host.getId(), LocalDate.now().plusDays(1));

        //when  then
        mockMvc.perform(post("/api/spaces/reserve")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("이용 마감 시간 필수"));
    }

    @Test
    @DisplayName("예약 등록 유효성 검사 - 가격 데이터가 음수")
    void invalid_create_reservation_price_value_minus() throws Exception {
        //given
        String requestJson = String.format("""
            {
                "hostId": %d,
                "spaceId": %d,
                "date": "%s",
                "startTime": "13:00:00",
                "endTime": "14:00:00",
                "price": -15000,
                "guestCount": 2,
                "usagePurpose": "BARISTA_TRAINING",
                "requestMessage": "요청 사항 테스트"
            }
            """, space.getId(), host.getId(), LocalDate.now().plusDays(1));

        //when  then
        mockMvc.perform(post("/api/spaces/reserve")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("가격은 0원부터 입력가능합니다."));
    }

    @Test
    @DisplayName("예약 등록 유효성 검사 - 인원 수 데이터가 0 이하일 경우")
    void invalid_create_reservation_guestCount_value_under_one() throws Exception {
        //given
        String requestJson = String.format("""
            {
                "hostId": %d,
                "spaceId": %d,
                "date": "%s",
                "startTime": "13:00:00",
                "endTime": "14:00:00",
                "price": 15000,
                "guestCount": 0,
                "usagePurpose": "BARISTA_TRAINING",
                "requestMessage": "요청 사항 테스트"
            }
            """, space.getId(), host.getId(), LocalDate.now().plusDays(1));

        //when  then
        mockMvc.perform(post("/api/spaces/reserve")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("최소 인원은 1명입니다."));
    }

    @Test
    @DisplayName("예약 등록 유효성 검사 - 이용 목적 공백")
    void invalid_create_reservation_usagepurpose_empty() throws Exception {
        //given
        String requestJson = String.format("""
            {
                "hostId": %d,
                "spaceId": %d,
                "date": "%s",
                "startTime": "13:00:00",
                "endTime": "14:00:00",
                "price": 15000,
                "guestCount": 2,
                "usagePurpose": null,
                "requestMessage": "요청 사항 테스트"
            }
            """, space.getId(), host.getId(), LocalDate.now().plusDays(1));

        //when  then
        mockMvc.perform(post("/api/spaces/reserve")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("이용 목적 필수"));
    }

    @Test
    @DisplayName("예약 등록 유효성 검사 - 요청 사항 200자 초과")
    void invalid_create_reservation_requestmessage_over_200() throws Exception {
        //given
        String requestJson = String.format("""
            {
                "hostId": %d,
                "spaceId": %d,
                "date": "%s",
                "startTime": "13:00:00",
                "endTime": "14:00:00",
                "price": 15000,
                "guestCount": 2,
                "usagePurpose": "BARISTA_TRAINING",
                "requestMessage": "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901"
            }
            """, space.getId(), host.getId(), LocalDate.now().plusDays(1));

        //when  then
        mockMvc.perform(post("/api/spaces/reserve")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("요청 사항은 200자 이내로 입력해주세요."));
    }

    @Test
    @DisplayName("이용가능 시간 체크 유효성 검사 - 공간 id 값 공백")
    void invalid_check_available_times_space_id_empty() throws Exception {
        //given
        String requestJson = String.format("""
            {
                "spaceId": null,
                "date": "%s"
            }
            """, LocalDate.now().plusDays(1));

        //when  then
        mockMvc.perform(post("/api/spaces/reserve/available-times")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("공간 id 입력은 필수입니다."));
    }

    @Test
    @DisplayName("이용가능 시간 체크 유효성 검사 - 날짜 데이터 공백")
    void invalid_check_available_times_date_empty() throws Exception {
        //given
        String requestJson = String.format("""
            {
                "spaceId": %d,
                "date": null
            }
            """, space.getId());

        //when  then
        mockMvc.perform(post("/api/spaces/reserve/available-times")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("날짜 입력은 필수입니다."));
    }

}