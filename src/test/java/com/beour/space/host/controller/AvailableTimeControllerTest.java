package com.beour.space.host.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.beour.global.exception.error.errorcode.SpaceErrorCode;
import com.beour.global.exception.error.errorcode.UserErrorCode;
import com.beour.reservation.commons.entity.Reservation;
import com.beour.reservation.commons.enums.ReservationStatus;
import com.beour.reservation.commons.repository.ReservationRepository;
import com.beour.space.domain.entity.AvailableTime;
import com.beour.space.domain.entity.Description;
import com.beour.space.domain.entity.Space;
import com.beour.space.domain.enums.SpaceCategory;
import com.beour.space.domain.enums.UseCategory;
import com.beour.space.domain.repository.AvailableTimeRepository;
import com.beour.space.domain.repository.DescriptionRepository;
import com.beour.space.domain.repository.SpaceRepository;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AvailableTimeControllerTest {

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
    private AvailableTimeRepository availableTimeRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ObjectMapper objectMapper;

    private User host;
    private User otherHost;
    private Space hostSpace;
    private Space otherHostSpace;
    private Description description;
    private AvailableTime availableTime1;
    private AvailableTime availableTime2;
    private Reservation pendingReservation;
    private Reservation acceptedReservation;

    @BeforeEach
    void setUp() {
        host = User.builder()
                .loginId("host")
                .password(passwordEncoder.encode("hostpassword!"))
                .name("호스트")
                .nickname("host")
                .email("host@gmail.com")
                .phone("01012345678")
                .role("HOST")
                .build();
        userRepository.save(host);

        otherHost = User.builder()
                .loginId("otherhost")
                .password(passwordEncoder.encode("otherhostpassword!"))
                .name("다른호스트")
                .nickname("otherhost")
                .email("otherhost@gmail.com")
                .phone("01012345679")
                .role("HOST")
                .build();
        userRepository.save(otherHost);

        hostSpace = Space.builder()
                .host(host)
                .name("호스트 공간")
                .spaceCategory(SpaceCategory.COOKING)
                .useCategory(UseCategory.COOKING)
                .maxCapacity(5)
                .address("서울시 강남구")
                .detailAddress("투썸건물 1층")
                .pricePerHour(10000)
                .thumbnailUrl("https://example.img")
                .latitude(37.5665)
                .longitude(126.9780)
                .avgRating(0.0)
                .availableTimes(new ArrayList<>())
                .build();
        spaceRepository.save(hostSpace);

        otherHostSpace = Space.builder()
                .host(otherHost)
                .name("다른 호스트 공간")
                .spaceCategory(SpaceCategory.CAFE)
                .useCategory(UseCategory.MEETING)
                .maxCapacity(3)
                .address("서울시 종로구")
                .detailAddress("스타벅스 2층")
                .pricePerHour(15000)
                .thumbnailUrl("https://example2.img")
                .latitude(37.5700)
                .longitude(126.9850)
                .avgRating(0.0)
                .availableTimes(new ArrayList<>())
                .build();
        spaceRepository.save(otherHostSpace);

        description = Description.builder()
                .space(hostSpace)
                .description("공간 설명")
                .priceGuide("가격 안내")
                .facilityNotice("시설 안내")
                .notice("주의사항")
                .locationDescription("위치 설명")
                .refundPolicy("환불 정책")
                .build();
        descriptionRepository.save(description);

        availableTime1 = AvailableTime.builder()
                .space(hostSpace)
                .date(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(12, 0))
                .build();
        availableTimeRepository.save(availableTime1);

        availableTime2 = AvailableTime.builder()
                .space(hostSpace)
                .date(LocalDate.now().plusDays(2))
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(18, 0))
                .build();
        availableTimeRepository.save(availableTime2);

        // 예약 중인 시간 (수정 불가능)
        pendingReservation = Reservation.builder()
                .space(hostSpace)
                .guest(host) // 테스트용으로 같은 사용자 사용
                .date(LocalDate.now().plusDays(3))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(13, 0))
                .status(ReservationStatus.PENDING)
                .price(30000)
                .build();
        reservationRepository.save(pendingReservation);

        acceptedReservation = Reservation.builder()
                .space(hostSpace)
                .guest(host) // 테스트용으로 같은 사용자 사용
                .date(LocalDate.now().plusDays(4))
                .startTime(LocalTime.of(15, 0))
                .endTime(LocalTime.of(17, 0))
                .status(ReservationStatus.ACCEPTED)
                .price(20000)
                .build();
        reservationRepository.save(acceptedReservation);
    }

    @AfterEach
    void tearDown() {
        reservationRepository.deleteAll();
        availableTimeRepository.deleteAll();
        descriptionRepository.deleteAll();
        spaceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("가능한 시간 조회 - 성공")
    @WithMockUser(username = "host")
    void success_getAvailableTimeDetail() throws Exception {
        // when & then
        mockMvc.perform(get("/api/spaces/{spaceId}/available-times", hostSpace.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.spaceId").value(hostSpace.getId()))
                .andExpect(jsonPath("$.data.editableTimeSlots.length()").value(2))
                .andExpect(jsonPath("$.data.editableTimeSlots[0].date").value(availableTime1.getDate().toString()))
                .andExpect(jsonPath("$.data.editableTimeSlots[0].startTime").value("09:00:00"))
                .andExpect(jsonPath("$.data.editableTimeSlots[0].endTime").value("12:00:00"))
                .andExpect(jsonPath("$.data.nonEditableTimeSlots.length()").value(2))
                .andExpect(jsonPath("$.data.nonEditableTimeSlots[0].date").value(pendingReservation.getDate().toString()))
                .andExpect(jsonPath("$.data.nonEditableTimeSlots[0].startTime").value("10:00:00"))
                .andExpect(jsonPath("$.data.nonEditableTimeSlots[0].endTime").value("13:00:00"));
    }

    @Test
    @DisplayName("가능한 시간 조회 - 존재하지 않는 공간")
    @WithMockUser(username = "host")
    void getAvailableTimeDetail_space_not_found() throws Exception {
        // given
        Long nonExistentSpaceId = 999L;

        // when & then
        mockMvc.perform(get("/api/spaces/{spaceId}/available-times", nonExistentSpaceId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(SpaceErrorCode.SPACE_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("가능한 시간 조회 - 권한 없음 (다른 호스트의 공간)")
    @WithMockUser(username = "host")
    void getAvailableTimeDetail_no_permission() throws Exception {
        // when & then
        mockMvc.perform(get("/api/spaces/{spaceId}/available-times", otherHostSpace.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(SpaceErrorCode.NO_PERMISSION.getMessage()));
    }

    @Test
    @DisplayName("가능한 시간 조회 - 인증되지 않은 사용자")
    void getAvailableTimeDetail_user_not_found() throws Exception {
        // when & then
        mockMvc.perform(get("/api/spaces/{spaceId}/available-times", hostSpace.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("가능한 시간 수정 - 성공")
    @WithMockUser(username = "host")
    void success_updateAvailableTimes() throws Exception {
        // given
        String requestJson = """
            {
                "availableTimes": [
                    {
                        "date": "%s",
                        "startTime": "10:00:00",
                        "endTime": "14:00:00"
                    },
                    {
                        "date": "%s",
                        "startTime": "16:00:00",
                        "endTime": "20:00:00"
                    }
                ]
            }
            """.formatted(
                LocalDate.now().plusDays(5).toString(),
                LocalDate.now().plusDays(6).toString()
        );

        // when & then
        mockMvc.perform(patch("/api/spaces/{spaceId}/available-times", hostSpace.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("대여 가능 시간을 성공적으로 업데이트 했습니다."));
    }

    @Test
    @DisplayName("가능한 시간 수정 - 존재하지 않는 공간")
    @WithMockUser(username = "host")
    void updateAvailableTimes_space_not_found() throws Exception {
        // given
        Long nonExistentSpaceId = 999L;
        String requestJson = """
            {
                "availableTimes": [
                    {
                        "date": "%s",
                        "startTime": "10:00:00",
                        "endTime": "14:00:00"
                    }
                ]
            }
            """.formatted(LocalDate.now().plusDays(1).toString());

        // when & then
        mockMvc.perform(patch("/api/spaces/{spaceId}/available-times", nonExistentSpaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(SpaceErrorCode.SPACE_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("가능한 시간 수정 - 권한 없음 (다른 호스트의 공간)")
    @WithMockUser(username = "host")
    void updateAvailableTimes_no_permission() throws Exception {
        // given
        String requestJson = """
            {
                "availableTimes": [
                    {
                        "date": "%s",
                        "startTime": "10:00:00",
                        "endTime": "14:00:00"
                    }
                ]
            }
            """.formatted(LocalDate.now().plusDays(1).toString());

        // when & then
        mockMvc.perform(patch("/api/spaces/{spaceId}/available-times", otherHostSpace.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(SpaceErrorCode.NO_PERMISSION.getMessage()));
    }

    @Test
    @DisplayName("가능한 시간 수정 - 인증되지 않은 사용자")
    void updateAvailableTimes_user_not_found() throws Exception {
        // given
        String requestJson = """
            {
                "availableTimes": [
                    {
                        "date": "%s",
                        "startTime": "10:00:00",
                        "endTime": "14:00:00"
                    }
                ]
            }
            """.formatted(LocalDate.now().plusDays(1).toString());

        // when & then
        mockMvc.perform(patch("/api/spaces/{spaceId}/available-times", hostSpace.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("가능한 시간 수정 - 잘못된 JSON 형식")
    @WithMockUser(username = "host")
    void updateAvailableTimes_invalid_json() throws Exception {
        // given
        String invalidJson = """
            {
                "availableTimes": [
                    {
                        "date": "invalid-date",
                        "startTime": "invalid-time",
                        "endTime": "invalid-time"
                    }
                ]
            }
            """;

        // when & then
        mockMvc.perform(patch("/api/spaces/{spaceId}/available-times", hostSpace.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("가능한 시간 수정 - 빈 시간 목록으로 업데이트")
    @WithMockUser(username = "host")
    void success_updateAvailableTimes_empty_list() throws Exception {
        // given
        String requestJson = """
            {
                "availableTimes": []
            }
            """;

        // when & then
        mockMvc.perform(patch("/api/spaces/{spaceId}/available-times", hostSpace.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("대여 가능 시간을 성공적으로 업데이트 했습니다."));
    }
}
