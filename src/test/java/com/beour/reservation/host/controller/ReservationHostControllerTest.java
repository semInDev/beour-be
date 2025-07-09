package com.beour.reservation.host.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.beour.global.jwt.JWTUtil;
import com.beour.reservation.commons.entity.Reservation;
import com.beour.reservation.commons.enums.ReservationStatus;
import com.beour.reservation.commons.enums.UsagePurpose;
import com.beour.reservation.commons.repository.ReservationRepository;
import com.beour.space.domain.entity.AvailableTime;
import com.beour.space.domain.entity.Space;
import com.beour.space.domain.enums.SpaceCategory;
import com.beour.space.domain.enums.UseCategory;
import com.beour.space.domain.repository.AvailableTimeRepository;
import com.beour.space.domain.repository.SpaceRepository;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ReservationHostControllerTest {

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
    private Space space1;
    private Space space2;
    private String hostAccessToken;
    private String guestAccessToken;

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
                .maxCapacity(5)
                .address("서울시 강남구")
                .detailAddress("투썸건물 2층")
                .pricePerHour(15000)
                .thumbnailUrl("https://example.img")
                .latitude(123.12)
                .longitude(123.12)
                .avgRating(0.0)
                .availableTimes(new ArrayList<>())
                .build();
        spaceRepository.save(space1);

        space2 = Space.builder()
                .host(host)
                .name("공간2")
                .spaceCategory(SpaceCategory.CAFE)
                .useCategory(UseCategory.MEETING)
                .maxCapacity(10)
                .address("서울시 서초구")
                .detailAddress("스터디카페 3층")
                .pricePerHour(20000)
                .thumbnailUrl("https://example2.img")
                .latitude(124.12)
                .longitude(124.12)
                .avgRating(0.0)
                .availableTimes(new ArrayList<>())
                .build();
        spaceRepository.save(space2);

        hostAccessToken = jwtUtil.createJwt(
                "access",
                host.getLoginId(),
                "ROLE_" + host.getRole(),
                1000L * 60 * 30    // 30분
        );

        guestAccessToken = jwtUtil.createJwt(
                "access",
                guest.getLoginId(),
                "ROLE_" + guest.getRole(),
                1000L * 60 * 30    // 30분
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        reservationRepository.deleteAll();
        availableTimeRepository.deleteAll();
        spaceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("호스트 공간 목록 조회 - 성공")
    void getHostSpaces_success() throws Exception {
        //when then
        mockMvc.perform(get("/api/host/spaces")
                        .header("Authorization", "Bearer " + hostAccessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].spaceName").value("공간1"))
                .andExpect(jsonPath("$.data[0].spaceId").value(space1.getId()))
                .andExpect(jsonPath("$.data[1].spaceName").value("공간2"))
                .andExpect(jsonPath("$.data[1].spaceId").value(space2.getId()));
    }

    @Test
    @DisplayName("호스트 공간 목록 조회 - 등록된 공간이 없을 경우")
    void getHostSpaces_noSpaces() throws Exception {
        //given
        spaceRepository.deleteAll();

        //when then
        mockMvc.perform(get("/api/host/spaces")
                        .header("Authorization", "Bearer " + hostAccessToken)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("해당 호스트가 등록한 공간이 없습니다."));
    }

    @Test
    @DisplayName("호스트 공간 목록 조회 - 게스트 권한으로 접근")
    void getHostSpaces_guestAccess() throws Exception {
        //when then
        mockMvc.perform(get("/api/host/spaces")
                        .header("Authorization", "Bearer " + guestAccessToken)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("해당 호스트가 등록한 공간이 없습니다."));
    }

/*    @Test
    @DisplayName("특정 날짜 호스트 예약 목록 조회 - 성공")
    void getHostReservationsByDate_success() throws Exception {
        //given
        LocalDate targetDate = LocalDate.now();
        LocalTime now = LocalTime.now();
        LocalTime startTime1 = now.minusMinutes(30);
        LocalTime endTime1 = now.plusMinutes(30);

        Reservation reservation1 = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space1)
                .status(ReservationStatus.ACCEPTED)
                .usagePurpose(UsagePurpose.COOKING_PRACTICE)
                .requestMessage("요리 연습 부탁드립니다.")
                .date(targetDate)
                .startTime(startTime1)
                .endTime(endTime1)
                .price(30000)
                .guestCount(3)
                .build();
        reservationRepository.save(reservation1);

        Reservation reservation2 = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space2)
                .status(ReservationStatus.ACCEPTED)
                .usagePurpose(UsagePurpose.GROUP_MEETING)
                .requestMessage("단체 모임 예약입니다.")
                .date(targetDate)
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(16, 0))
                .price(40000)
                .guestCount(8)
                .build();
        reservationRepository.save(reservation2);

        //when then
        mockMvc.perform(get("/api/host/reservations")
                        .param("date", targetDate.toString())
                        .header("Authorization", "Bearer " + hostAccessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].reservationId").value(reservation1.getId()))
                .andExpect(jsonPath("$.data[0].guestName").value("게스트"))
                .andExpect(jsonPath("$.data[0].status").value("ACCEPTED"))
                .andExpect(jsonPath("$.data[0].spaceName").value("공간1"))
                .andExpect(jsonPath("$.data[0].startTime").value(startTime1.truncatedTo(ChronoUnit.MILLIS).toString()))
                .andExpect(jsonPath("$.data[0].endTime").value(endTime1.truncatedTo(ChronoUnit.MILLIS).toString()))
                .andExpect(jsonPath("$.data[0].guestCount").value(3))
                .andExpect(jsonPath("$.data[?(@.isCurrentlyInUse == true)]").exists()) // 현재 사용 중인 예약이 존재하는지 확인
                .andExpect(jsonPath("$.data[1].reservationId").value(reservation2.getId()))
                .andExpect(jsonPath("$.data[1].spaceName").value("공간2"));
    }*/

    @Test
    @DisplayName("특정 날짜 호스트 예약 목록 조회 - 확정되지 않은 예약 제외")
    void getHostReservationsByDate_onlyAcceptedReservations() throws Exception {
        //given
        LocalDate targetDate = LocalDate.now().plusDays(1);

        // ACCEPTED 상태 예약
        Reservation acceptedReservation = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space1)
                .status(ReservationStatus.ACCEPTED)
                .usagePurpose(UsagePurpose.COOKING_PRACTICE)
                .requestMessage("요리 연습 부탁드립니다.")
                .date(targetDate)
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(12, 0, 0))
                .price(30000)
                .guestCount(3)
                .build();
        reservationRepository.save(acceptedReservation);

        // PENDING 상태 예약 (결과에 포함되지 않아야 함)
        Reservation pendingReservation = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space1)
                .status(ReservationStatus.PENDING)
                .usagePurpose(UsagePurpose.COOKING_PRACTICE)
                .requestMessage("대기 중인 예약")
                .date(targetDate)
                .startTime(LocalTime.of(14, 0, 0))
                .endTime(LocalTime.of(16, 0, 0))
                .price(30000)
                .guestCount(2)
                .build();
        reservationRepository.save(pendingReservation);

        //when then
        mockMvc.perform(get("/api/host/reservations")
                        .param("date", targetDate.toString())
                        .header("Authorization", "Bearer " + hostAccessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].reservationId").value(acceptedReservation.getId()))
                .andExpect(jsonPath("$.data[0].status").value("ACCEPTED"));
    }

    @Test
    @DisplayName("특정 날짜 호스트 예약 목록 조회 - 확정된 예약이 없을 경우")
    void getHostReservationsByDate_noAcceptedReservations() throws Exception {
        //given
        LocalDate targetDate = LocalDate.now().plusDays(1);

        //when then
        mockMvc.perform(get("/api/host/reservations")
                        .param("date", targetDate.toString())
                        .header("Authorization", "Bearer " + hostAccessToken)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("해당 날짜에 확정된 예약이 없습니다."));
    }

/*    @Test
    @DisplayName("특정 날짜 호스트 예약 목록 조회 - 현재 사용 중인 예약 확인")
    void getHostReservationsByDate_currentlyInUse() throws Exception {
        //given
        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        // 현재 시간 기준으로 사용 중인 예약 (시작 시간 < 현재 시간 < 종료 시간)
        Reservation currentReservation = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space1)
                .status(ReservationStatus.ACCEPTED)
                .usagePurpose(UsagePurpose.COOKING_PRACTICE)
                .requestMessage("현재 사용 중")
                .date(today)
                .startTime(currentTime.minusHours(1))
                .endTime(currentTime.plusHours(1))
                .price(30000)
                .guestCount(3)
                .build();
        reservationRepository.save(currentReservation);

        //when then
        mockMvc.perform(get("/api/host/reservations")
                        .param("date", today.toString())
                        .header("Authorization", "Bearer " + hostAccessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[?(@.isCurrentlyInUse == true)]").exists());
    }*/

    @Test
    @DisplayName("특정 날짜와 공간의 호스트 예약 목록 조회 - 성공")
    void getHostReservationsByDateAndSpace_success() throws Exception {
        //given
        LocalDate targetDate = LocalDate.now().plusDays(1);

        // space1에 대한 예약
        Reservation reservation1 = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space1)
                .status(ReservationStatus.ACCEPTED)
                .usagePurpose(UsagePurpose.COOKING_PRACTICE)
                .requestMessage("요리 연습 부탁드립니다.")
                .date(targetDate)
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(12, 0, 0))
                .price(30000)
                .guestCount(3)
                .build();
        reservationRepository.save(reservation1);

        // space2에 대한 예약 (결과에 포함되지 않아야 함)
        Reservation reservation2 = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space2)
                .status(ReservationStatus.ACCEPTED)
                .usagePurpose(UsagePurpose.GROUP_MEETING)
                .requestMessage("단체 모임 예약입니다.")
                .date(targetDate)
                .startTime(LocalTime.of(14, 0, 0))
                .endTime(LocalTime.of(16, 0, 0))
                .price(40000)
                .guestCount(8)
                .build();
        reservationRepository.save(reservation2);

        //when then
        mockMvc.perform(get("/api/host/reservations/space")
                        .param("date", targetDate.toString())
                        .param("spaceId", space1.getId().toString())
                        .header("Authorization", "Bearer " + hostAccessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].reservationId").value(reservation1.getId()))
                .andExpect(jsonPath("$.data[0].spaceName").value("공간1"));
    }

    @Test
    @DisplayName("특정 날짜와 공간의 호스트 예약 목록 조회 - 존재하지 않는 공간")
    void getHostReservationsByDateAndSpace_spaceNotFound() throws Exception {
        //given
        LocalDate targetDate = LocalDate.now().plusDays(1);

        //when then
        mockMvc.perform(get("/api/host/reservations/space")
                        .param("date", targetDate.toString())
                        .param("spaceId", "999")
                        .header("Authorization", "Bearer " + hostAccessToken)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 공간입니다."));
    }

    @Test
    @DisplayName("특정 날짜와 공간의 호스트 예약 목록 조회 - 공간 소유자가 아닌 경우")
    void getHostReservationsByDateAndSpace_notSpaceOwner() throws Exception {
        //given
        LocalDate targetDate = LocalDate.now().plusDays(1);

        // 다른 호스트 생성
        User otherHost = User.builder()
                .loginId("otherHost")
                .password(passwordEncoder.encode("password!"))
                .name("다른호스트")
                .nickname("otherHost")
                .email("other@gmail.com")
                .phone("01098765432")
                .role("HOST")
                .build();
        userRepository.save(otherHost);

        // 다른 호스트의 공간 생성
        Space otherSpace = Space.builder()
                .host(otherHost)
                .name("다른공간")
                .spaceCategory(SpaceCategory.COOKING)
                .useCategory(UseCategory.COOKING)
                .maxCapacity(5)
                .address("서울시 마포구")
                .detailAddress("다른건물 1층")
                .pricePerHour(10000)
                .thumbnailUrl("https://other.img")
                .latitude(125.12)
                .longitude(125.12)
                .avgRating(0.0)
                .availableTimes(new ArrayList<>())
                .build();
        spaceRepository.save(otherSpace);

        //when then
        mockMvc.perform(get("/api/host/reservations/space")
                        .param("date", targetDate.toString())
                        .param("spaceId", otherSpace.getId().toString())
                        .header("Authorization", "Bearer " + hostAccessToken)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("해당 공간의 소유자가 아닙니다."));
    }

    @Test
    @DisplayName("특정 날짜와 공간의 호스트 예약 목록 조회 - 확정된 예약이 없을 경우")
    void getHostReservationsByDateAndSpace_noAcceptedReservations() throws Exception {
        //given
        LocalDate targetDate = LocalDate.now().plusDays(1);

        //when then
        mockMvc.perform(get("/api/host/reservations/space")
                        .param("date", targetDate.toString())
                        .param("spaceId", space1.getId().toString())
                        .header("Authorization", "Bearer " + hostAccessToken)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("해당 날짜와 공간에 확정된 예약이 없습니다."));
    }

    @Test
    @DisplayName("soft delete된 예약은 조회되지 않음")
    void getHostReservations_excludeSoftDeletedReservations() throws Exception {
        //given
        LocalDate targetDate = LocalDate.now().plusDays(1);

        // 정상 예약
        Reservation normalReservation = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space1)
                .status(ReservationStatus.ACCEPTED)
                .usagePurpose(UsagePurpose.COOKING_PRACTICE)
                .requestMessage("정상 예약")
                .date(targetDate)
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(12, 0, 0))
                .price(30000)
                .guestCount(3)
                .build();
        reservationRepository.save(normalReservation);

        // soft delete된 예약
        Reservation deletedReservation = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space1)
                .status(ReservationStatus.ACCEPTED)
                .usagePurpose(UsagePurpose.COOKING_PRACTICE)
                .requestMessage("삭제된 예약")
                .date(targetDate)
                .startTime(LocalTime.of(14, 0, 0))
                .endTime(LocalTime.of(16, 0, 0))
                .price(30000)
                .guestCount(2)
                .build();
        reservationRepository.save(deletedReservation);
        deletedReservation.softDelete(); // soft delete 실행
        reservationRepository.save(deletedReservation);

        //when then
        mockMvc.perform(get("/api/host/reservations")
                        .param("date", targetDate.toString())
                        .header("Authorization", "Bearer " + hostAccessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].reservationId").value(normalReservation.getId()));
    }
}
