package com.beour.reservation.host.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.beour.global.exception.error.errorcode.ReservationErrorCode;
import com.beour.global.exception.error.errorcode.SpaceErrorCode;
import com.beour.global.jwt.JWTUtil;
import com.beour.reservation.commons.entity.Reservation;
import com.beour.reservation.commons.enums.ReservationStatus;
import com.beour.reservation.commons.enums.UsagePurpose;
import com.beour.reservation.commons.repository.ReservationRepository;
import com.beour.space.domain.entity.Space;
import com.beour.space.domain.enums.SpaceCategory;
import com.beour.space.domain.enums.UseCategory;
import com.beour.space.domain.repository.AvailableTimeRepository;
import com.beour.space.domain.repository.SpaceRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ReservationCalendarControllerTest {

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
    @Autowired
    private AvailableTimeRepository availableTimeRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    private User guest;
    private User host;
    private User anotherHost;
    private Space space;
    private Space anotherSpace;
    private String hostAccessToken;
    private String anotherHostAccessToken;
    private Reservation pendingReservation;
    private Reservation acceptedReservation;
    private Reservation rejectedReservation;

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

        anotherHost = User.builder()
                .loginId("host2")
                .password(passwordEncoder.encode("host2password!"))
                .name("호스트2")
                .nickname("host2")
                .email("host2@gmail.com")
                .phone("01012345679")
                .role("HOST")
                .build();
        userRepository.save(anotherHost);

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

        anotherSpace = Space.builder()
                .host(anotherHost)
                .name("공간2")
                .spaceCategory(SpaceCategory.COOKING)
                .useCategory(UseCategory.COOKING)
                .maxCapacity(5)
                .address("서울시 강남구")
                .detailAddress("투썸건물 3층")
                .pricePerHour(20000)
                .thumbnailUrl("https://example.img")
                .latitude(123.12)
                .longitude(123.12)
                .avgRating(0.0)
                .availableTimes(new ArrayList<>())
                .build();
        spaceRepository.save(anotherSpace);

        pendingReservation = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space)
                .status(ReservationStatus.PENDING)
                .usagePurpose(UsagePurpose.BARISTA_TRAINING)
                .requestMessage("대기중인 예약")
                .date(LocalDate.now())
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(12, 0, 0))
                .price(30000)
                .guestCount(2)
                .build();
        reservationRepository.save(pendingReservation);

        acceptedReservation = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space)
                .status(ReservationStatus.ACCEPTED)
                .usagePurpose(UsagePurpose.COOKING_PRACTICE)
                .requestMessage("승인된 예약")
                .date(LocalDate.now())
                .startTime(LocalTime.of(14, 0, 0))
                .endTime(LocalTime.of(16, 0, 0))
                .price(30000)
                .guestCount(3)
                .build();
        reservationRepository.save(acceptedReservation);

        rejectedReservation = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space)
                .status(ReservationStatus.REJECTED)
                .usagePurpose(UsagePurpose.OTHER)
                .requestMessage("거부된 예약")
                .date(LocalDate.now())
                .startTime(LocalTime.of(18, 0, 0))
                .endTime(LocalTime.of(20, 0, 0))
                .price(30000)
                .guestCount(1)
                .build();
        reservationRepository.save(rejectedReservation);

        hostAccessToken = jwtUtil.createJwt(
                "access",
                host.getLoginId(),
                "ROLE_" + host.getRole(),
                1000L * 60 * 30    // 30분
        );

        anotherHostAccessToken = jwtUtil.createJwt(
                "access",
                anotherHost.getLoginId(),
                "ROLE_" + anotherHost.getRole(),
                1000L * 60 * 30    // 30분
        );
    }

    @AfterEach
    void tearDown() {
        reservationRepository.deleteAll();
        availableTimeRepository.deleteAll();
        spaceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("호스트 캘린더 예약 조회 - 전체 예약 조회 성공")
    void getHostCalendarReservations_success() throws Exception {
        mockMvc.perform(get("/api/host/calendar/reservations")
                        .param("date", LocalDate.now().toString())
                        .header("Authorization", "Bearer " + hostAccessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].guestName").value("게스트"))
                .andExpect(jsonPath("$.data[0].spaceName").value("공간1"));
    }

    @Test
    @DisplayName("호스트 캘린더 예약 조회 - 특정 공간 예약 조회 성공")
    void getHostCalendarReservations_with_spaceId_success() throws Exception {
        mockMvc.perform(get("/api/host/calendar/reservations")
                        .param("date", LocalDate.now().toString())
                        .param("spaceId", space.getId().toString())
                        .header("Authorization", "Bearer " + hostAccessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].spaceName").value("공간1"));
    }

    @Test
    @DisplayName("호스트 캘린더 예약 조회 - 다른 호스트 공간 조회 시 권한 오류")
    void getHostCalendarReservations_unauthorized_space() throws Exception {
        mockMvc.perform(get("/api/host/calendar/reservations")
                        .param("date", LocalDate.now().toString())
                        .param("spaceId", anotherSpace.getId().toString())
                        .header("Authorization", "Bearer " + hostAccessToken)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(SpaceErrorCode.NO_PERMISSION.getMessage()));
    }

    @Test
    @DisplayName("호스트 캘린더 예약 조회 - 존재하지 않는 공간")
    void getHostCalendarReservations_space_not_found() throws Exception {
        mockMvc.perform(get("/api/host/calendar/reservations")
                        .param("date", LocalDate.now().toString())
                        .param("spaceId", "999")
                        .header("Authorization", "Bearer " + hostAccessToken)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 공간입니다."));
    }

    @Test
    @DisplayName("호스트 대기중 예약 조회 - 성공")
    void getHostPendingReservations_success() throws Exception {
        mockMvc.perform(get("/api/host/calendar/reservations/pending")
                        .param("date", LocalDate.now().toString())
                        .header("Authorization", "Bearer " + hostAccessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"))
                .andExpect(jsonPath("$.data[0].guestName").value("게스트"));
    }

    @Test
    @DisplayName("호스트 승인된 예약 조회 - 성공")
    void getHostAcceptedReservations_success() throws Exception {
        mockMvc.perform(get("/api/host/calendar/reservations/accepted")
                        .param("date", LocalDate.now().toString())
                        .header("Authorization", "Bearer " + hostAccessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].status").value("ACCEPTED"))
                .andExpect(jsonPath("$.data[0].guestName").value("게스트"));
    }

    @Test
    @DisplayName("호스트 대기중 예약 조회 - 특정 공간으로 조회")
    void getHostPendingReservations_with_spaceId() throws Exception {
        mockMvc.perform(get("/api/host/calendar/reservations/pending")
                        .param("date", LocalDate.now().toString())
                        .param("spaceId", space.getId().toString())
                        .header("Authorization", "Bearer " + hostAccessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));
    }

    @Test
    @Transactional
    @DisplayName("예약 승인 - 성공")
    void acceptReservation_success() throws Exception {
        mockMvc.perform(patch("/api/host/calendar/reservations/accept")
                        .param("reservationId", pendingReservation.getId().toString())
                        .param("spaceId", space.getId().toString())
                        .header("Authorization", "Bearer " + hostAccessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("예약이 승인되었습니다."));

        Reservation updatedReservation = reservationRepository.findById(pendingReservation.getId()).get();
        assertEquals(ReservationStatus.ACCEPTED, updatedReservation.getStatus());
    }

    @Test
    @DisplayName("예약 승인 - 존재하지 않는 예약")
    void acceptReservation_reservation_not_found() throws Exception {
        mockMvc.perform(patch("/api/host/calendar/reservations/accept")
                        .param("reservationId", "999")
                        .param("spaceId", space.getId().toString())
                        .header("Authorization", "Bearer " + hostAccessToken)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ReservationErrorCode.RESERVATION_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("예약 승인 - 존재하지 않는 공간")
    void acceptReservation_space_not_found() throws Exception {
        mockMvc.perform(patch("/api/host/calendar/reservations/accept")
                        .param("reservationId", pendingReservation.getId().toString())
                        .param("spaceId", "999")
                        .header("Authorization", "Bearer " + hostAccessToken)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 공간입니다."));
    }

    @Test
    @DisplayName("예약 승인 - 다른 호스트의 공간")
    void acceptReservation_unauthorized_space() throws Exception {
        mockMvc.perform(patch("/api/host/calendar/reservations/accept")
                        .param("reservationId", pendingReservation.getId().toString())
                        .param("spaceId", anotherSpace.getId().toString())
                        .header("Authorization", "Bearer " + hostAccessToken)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(SpaceErrorCode.NO_PERMISSION.getMessage()));
    }

    @Test
    @DisplayName("예약 승인 - 예약과 공간 정보 불일치")
    void acceptReservation_reservation_space_mismatch() throws Exception {
        // 다른 호스트의 공간에 대한 예약 생성
        Reservation anotherReservation = Reservation.builder()
                .guest(guest)
                .host(anotherHost)
                .space(anotherSpace)
                .status(ReservationStatus.PENDING)
                .usagePurpose(UsagePurpose.OTHER)
                .requestMessage("다른 공간 예약")
                .date(LocalDate.now())
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(12, 0, 0))
                .price(40000)
                .guestCount(2)
                .build();
        reservationRepository.save(anotherReservation);

        mockMvc.perform(patch("/api/host/calendar/reservations/accept")
                        .param("reservationId", anotherReservation.getId().toString())
                        .param("spaceId", space.getId().toString())
                        .header("Authorization", "Bearer " + hostAccessToken)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("예약과 공간 정보가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("예약 승인 - 다른 호스트의 예약")
    void acceptReservation_unauthorized_reservation() throws Exception {
        Reservation anotherReservation = Reservation.builder()
                .guest(guest)
                .host(anotherHost)
                .space(anotherSpace)
                .status(ReservationStatus.PENDING)
                .usagePurpose(UsagePurpose.OTHER)
                .requestMessage("다른 호스트 예약")
                .date(LocalDate.now())
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(12, 0, 0))
                .price(40000)
                .guestCount(2)
                .build();
        reservationRepository.save(anotherReservation);

        mockMvc.perform(patch("/api/host/calendar/reservations/accept")
                        .param("reservationId", anotherReservation.getId().toString())
                        .param("spaceId", anotherSpace.getId().toString())
                        .header("Authorization", "Bearer " + hostAccessToken)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(SpaceErrorCode.NO_PERMISSION.getMessage()));
    }

    @Test
    @Transactional
    @DisplayName("예약 거부 - 성공")
    void rejectReservation_success() throws Exception {
        mockMvc.perform(patch("/api/host/calendar/reservations/reject")
                        .param("reservationId", pendingReservation.getId().toString())
                        .param("spaceId", space.getId().toString())
                        .header("Authorization", "Bearer " + hostAccessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("예약이 거부되었습니다."));

        Reservation updatedReservation = reservationRepository.findById(pendingReservation.getId()).get();
        assertEquals(ReservationStatus.REJECTED, updatedReservation.getStatus());
    }

    @Test
    @DisplayName("예약 거부 - 존재하지 않는 예약")
    void rejectReservation_reservation_not_found() throws Exception {
        mockMvc.perform(patch("/api/host/calendar/reservations/reject")
                        .param("reservationId", "999")
                        .param("spaceId", space.getId().toString())
                        .header("Authorization", "Bearer " + hostAccessToken)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ReservationErrorCode.RESERVATION_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("예약 거부 - 존재하지 않는 공간")
    void rejectReservation_space_not_found() throws Exception {
        mockMvc.perform(patch("/api/host/calendar/reservations/reject")
                        .param("reservationId", pendingReservation.getId().toString())
                        .param("spaceId", "999")
                        .header("Authorization", "Bearer " + hostAccessToken)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 공간입니다."));
    }

    @Test
    @DisplayName("예약 거부 - 다른 호스트의 공간")
    void rejectReservation_unauthorized_space() throws Exception {
        mockMvc.perform(patch("/api/host/calendar/reservations/reject")
                        .param("reservationId", pendingReservation.getId().toString())
                        .param("spaceId", anotherSpace.getId().toString())
                        .header("Authorization", "Bearer " + hostAccessToken)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(SpaceErrorCode.NO_PERMISSION.getMessage()));
    }
}