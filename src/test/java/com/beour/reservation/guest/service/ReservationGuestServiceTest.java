package com.beour.reservation.guest.service;

import static org.junit.jupiter.api.Assertions.*;

import com.beour.global.exception.exceptionType.SpaceNotFoundException;
import com.beour.global.exception.exceptionType.UserNotFoundException;
import com.beour.reservation.commons.entity.Reservation;
import com.beour.reservation.commons.enums.ReservationStatus;
import com.beour.reservation.commons.enums.UsagePurpose;
import com.beour.reservation.commons.exceptionType.AvailableTimeNotFound;
import com.beour.reservation.commons.exceptionType.MissMatch;
import com.beour.reservation.commons.repository.ReservationRepository;
import com.beour.reservation.guest.dto.ReservationCreateRequest;
import com.beour.reservation.guest.dto.ReservationResponseDto;
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

@ActiveProfiles("test")
@SpringBootTest
class ReservationGuestServiceTest {

    @Autowired
    private ReservationGuestService reservationGuestService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private SpaceRepository spaceRepository;
    @Autowired
    private AvailableTimeRepository availableTimeRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    private User guest;
    private User host;
    private Space space;
    private AvailableTime availableTimePast;
    private AvailableTime availableTimeCurrent;
    private AvailableTime availableTimeNext;


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

        availableTimePast = AvailableTime.builder()
            .space(space)
            .date(LocalDate.now().minusDays(1))
            .startTime(LocalTime.of(1, 0, 0))
            .endTime(LocalTime.of(23, 0, 0))
            .build();
        availableTimeRepository.save(availableTimePast);
        space.getAvailableTimes().add(availableTimePast);

        availableTimeCurrent = AvailableTime.builder()
            .space(space)
            .date(LocalDate.now())
            .startTime(LocalTime.of(1, 0, 0))
            .endTime(LocalTime.of(23, 0, 0))
            .build();
        availableTimeRepository.save(availableTimeCurrent);
        space.getAvailableTimes().add(availableTimeCurrent);

        availableTimeNext = AvailableTime.builder()
            .space(space)
            .date(LocalDate.now().plusDays(1))
            .startTime(LocalTime.of(1, 0, 0))
            .endTime(LocalTime.of(23, 0, 0))
            .build();
        availableTimeRepository.save(availableTimeNext);
        space.getAvailableTimes().add(availableTimeNext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        availableTimeRepository.deleteAll();
        reservationRepository.deleteAll();
        spaceRepository.deleteAll();
        userRepository.deleteAll();
    }


    /**
     * 예약 현황 조회
     * - 성공
     * - 과거 예약 잘 걸러지는지
     * - 현 시점의 시간 이전의 시간 잘 걸러지는지
     *
     * 지난 예약 조회
     * - 성공
     * - 시간 지나면 예약 사용 완료 상태로 변경
     *
     * 예약 취소
     * - 성공
     * - 존재하지 않는 예약일 경우
     * - 이미 확정된 예약일 경우
     */

    @Test
    @DisplayName("공간 예약 - 시간당 가격과 총 가격이 불일치할 경우")
    void create_reservation_not_same_price() {
        //given
        ReservationCreateRequest request = new ReservationCreateRequest(host.getId(), space.getId(),
            LocalDate.now().plusDays(1), LocalTime.of(13, 0, 0), LocalTime.of(15, 0, 0), 10000, 2,
            UsagePurpose.BARISTA_TRAINING, "테슽뚜");

        //when  then
        assertThrows(MissMatch.class, () -> reservationGuestService.createReservation(request));
    }

    @Test
    @DisplayName("공간 예약 - 과거의 날짜")
    void create_reservation_past_date() {
        //given
        ReservationCreateRequest request = new ReservationCreateRequest(host.getId(), space.getId(),
            LocalDate.now().minusDays(1), LocalTime.of(13, 0, 0), LocalTime.of(15, 0, 0), 30000, 2,
            UsagePurpose.BARISTA_TRAINING, "테슽뚜");

        //when  then
        assertThrows(AvailableTimeNotFound.class, () -> reservationGuestService.createReservation(request));
    }

    @Test
    @DisplayName("공간 예약 - 현 시점 이전의 시간")
    void create_reservation_past_time() {
        //given
        ReservationCreateRequest request = new ReservationCreateRequest(host.getId(), space.getId(),
            LocalDate.now(), LocalTime.of(17, 0, 0), LocalTime.of(18, 0, 0), 15000, 2,
            UsagePurpose.BARISTA_TRAINING, "테슽뚜");

        //when  then
        assertThrows(AvailableTimeNotFound.class, () -> reservationGuestService.createReservation(request));
    }

    @Test
    @DisplayName("공간 예약 - 인원 초과")
    void create_reservation_capacity_invalid() {
        //given
        ReservationCreateRequest request = new ReservationCreateRequest(host.getId(), space.getId(),
            LocalDate.now().plusDays(1), LocalTime.of(17, 0, 0), LocalTime.of(18, 0, 0), 15000, 10,
            UsagePurpose.BARISTA_TRAINING, "테슽뚜");

        //when  then
        assertThrows(MissMatch.class, () -> reservationGuestService.createReservation(request));
    }

    @Test
    @DisplayName("공간 예약 - 예약 시간이 다른 예약과 겹침")
    void create_reservation_duplicate_other_reserve() {
        //given
        Reservation reservation = Reservation.builder()
            .guest(guest)
            .host(host)
            .space(space)
            .status(ReservationStatus.ACCEPTED)
            .usagePurpose(UsagePurpose.BARISTA_TRAINING)
            .requestMessage("테슽뚜")
            .date(LocalDate.now().plusDays(1))
            .startTime(LocalTime.of(12, 0, 0))
            .endTime(LocalTime.of(16, 0, 0))
            .price(60000)
            .guestCount(2)
            .build();
        reservationRepository.save(reservation);

        ReservationCreateRequest request = new ReservationCreateRequest(host.getId(), space.getId(),
            LocalDate.now().plusDays(1), LocalTime.of(15, 0, 0), LocalTime.of(18, 0, 0), 45000, 2,
            UsagePurpose.BARISTA_TRAINING, "테슽뚜");

        //when  then
        assertThrows(AvailableTimeNotFound.class, () -> reservationGuestService.createReservation(request));
    }

    @Test
    @DisplayName("공간 예약 - 없는 호스트")
    void create_reservation_with_non_existent_host() {
        //given
        ReservationCreateRequest request = new ReservationCreateRequest(3L, space.getId(),
            LocalDate.now().plusDays(1), LocalTime.of(17, 0, 0), LocalTime.of(18, 0, 0), 15000, 10,
            UsagePurpose.BARISTA_TRAINING, "테슽뚜");

        //when  then
        assertThrows(UserNotFoundException.class, () -> reservationGuestService.createReservation(request));
    }

    @Test
    @DisplayName("공간 예약 - 없는 공간")
    void create_reservation_with_non_existent_space() {
        //given
        ReservationCreateRequest request = new ReservationCreateRequest(host.getId(), 3L,
            LocalDate.now().plusDays(1), LocalTime.of(17, 0, 0), LocalTime.of(18, 0, 0), 15000, 10,
            UsagePurpose.BARISTA_TRAINING, "테슽뚜");

        //when  then
        assertThrows(SpaceNotFoundException.class, () -> reservationGuestService.createReservation(request));
    }

    @Test
    @DisplayName("공간 예약 - 성공")
    void success_create_reservation() {
        //given
        ReservationCreateRequest request = new ReservationCreateRequest(host.getId(), space.getId(),
            LocalDate.now().plusDays(1), LocalTime.of(17, 0, 0), LocalTime.of(18, 0, 0), 15000, 2,
            UsagePurpose.BARISTA_TRAINING, "테슽뚜");

        //when
        ReservationResponseDto result = reservationGuestService.createReservation(request);

        //then
        Reservation savedReservation = reservationRepository.findById(result.getId()).orElse(null);
        assertEquals(request.getSpaceId(), savedReservation.getSpace().getId());
        assertEquals(request.getDate(), savedReservation.getDate());
        assertEquals(request.getStartTime(), savedReservation.getStartTime());
        assertEquals(request.getEndTime(), savedReservation.getEndTime());
        assertEquals(request.getRequestMessage(), savedReservation.getRequestMessage());
    }

}