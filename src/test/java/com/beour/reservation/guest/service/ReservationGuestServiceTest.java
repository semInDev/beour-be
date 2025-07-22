package com.beour.reservation.guest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.beour.global.exception.exceptionType.SpaceNotFoundException;
import com.beour.global.exception.exceptionType.UserNotFoundException;
import com.beour.reservation.commons.entity.Reservation;
import com.beour.reservation.commons.enums.ReservationStatus;
import com.beour.reservation.commons.enums.UsagePurpose;
import com.beour.reservation.commons.exceptionType.AvailableTimeNotFound;
import com.beour.reservation.commons.exceptionType.MissMatch;
import com.beour.reservation.commons.exceptionType.ReservationNotFound;
import com.beour.reservation.commons.repository.ReservationRepository;
import com.beour.reservation.guest.dto.ReservationCreateRequest;
import com.beour.reservation.guest.dto.ReservationListResponseDto;
import com.beour.reservation.guest.dto.ReservationResponseDto;
import com.beour.space.domain.entity.AvailableTime;
import com.beour.space.domain.entity.Space;
import com.beour.space.domain.repository.AvailableTimeRepository;
import com.beour.space.domain.repository.SpaceRepository;
import com.beour.space.domain.enums.SpaceCategory;
import com.beour.space.domain.enums.UseCategory;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import org.springframework.transaction.annotation.Transactional;

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

    @Test
    @DisplayName("공간 예약 - 시간당 가격과 총 가격이 불일치할 경우")
    void create_reservation_not_same_price() {
        //given
        ReservationCreateRequest request = new ReservationCreateRequest(LocalDate.now().plusDays(1), LocalTime.of(13, 0, 0), LocalTime.of(15, 0, 0), 10000, 2,
            UsagePurpose.BARISTA_TRAINING, "테슽뚜");

        //when  then
        assertThrows(MissMatch.class, () -> reservationGuestService.createReservation(space.getId(), request));
    }

    @Test
    @DisplayName("공간 예약 - 과거의 날짜")
    void create_reservation_past_date() {
        //given
        ReservationCreateRequest request = new ReservationCreateRequest(LocalDate.now().minusDays(1), LocalTime.of(13, 0, 0), LocalTime.of(15, 0, 0), 30000, 2,
            UsagePurpose.BARISTA_TRAINING, "테슽뚜");

        //when  then
        assertThrows(AvailableTimeNotFound.class,
            () -> reservationGuestService.createReservation(space.getId(), request));
    }

    @Test
    @DisplayName("공간 예약 - 현 시점 이전의 시간")
    void create_reservation_past_time() {
        //given
        int currentHour = LocalTime.now().getHour();
        ReservationCreateRequest request = new ReservationCreateRequest(LocalDate.now(), LocalTime.of(currentHour - 1, 0, 0),
            LocalTime.of(currentHour + 1, 0, 0), 30000, 2,
            UsagePurpose.BARISTA_TRAINING, "테슽뚜");

        //when  then
        assertThrows(AvailableTimeNotFound.class,
            () -> reservationGuestService.createReservation(space.getId(), request));
    }

    @Test
    @DisplayName("공간 예약 - 인원 초과")
    void create_reservation_capacity_invalid() {
        //given
        ReservationCreateRequest request = new ReservationCreateRequest(LocalDate.now().plusDays(1), LocalTime.of(17, 0, 0), LocalTime.of(18, 0, 0), 15000, 10,
            UsagePurpose.BARISTA_TRAINING, "테슽뚜");

        //when  then
        assertThrows(MissMatch.class, () -> reservationGuestService.createReservation(space.getId(), request));
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

        ReservationCreateRequest request = new ReservationCreateRequest(LocalDate.now().plusDays(1), LocalTime.of(15, 0, 0), LocalTime.of(18, 0, 0), 45000, 2,
            UsagePurpose.BARISTA_TRAINING, "테슽뚜");

        //when  then
        assertThrows(MissMatch.class,
            () -> reservationGuestService.createReservation(space.getId(), request));
    }

    @Test
    @DisplayName("공간 예약 - 없는 공간")
    void create_reservation_with_non_existent_space() {
        //given
        ReservationCreateRequest request = new ReservationCreateRequest(LocalDate.now().plusDays(1), LocalTime.of(17, 0, 0), LocalTime.of(18, 0, 0), 15000, 10,
            UsagePurpose.BARISTA_TRAINING, "테슽뚜");

        //when  then
        assertThrows(SpaceNotFoundException.class,
            () -> reservationGuestService.createReservation(3L, request));
    }

    @Test
    @DisplayName("공간 예약 - 성공")
    void success_create_reservation() {
        //given
        ReservationCreateRequest request = new ReservationCreateRequest(LocalDate.now().plusDays(1), LocalTime.of(17, 0, 0), LocalTime.of(18, 0, 0), 15000, 2,
            UsagePurpose.BARISTA_TRAINING, "테슽뚜");

        //when
        ReservationResponseDto result = reservationGuestService.createReservation(space.getId(), request);

        //then
        Reservation savedReservation = reservationRepository.findById(result.getId()).orElse(null);
        assertEquals(space.getId(), savedReservation.getSpace().getId());
        assertEquals(request.getDate(), savedReservation.getDate());
        assertEquals(request.getStartTime(), savedReservation.getStartTime());
        assertEquals(request.getEndTime(), savedReservation.getEndTime());
        assertEquals(request.getRequestMessage(), savedReservation.getRequestMessage());
    }

    @Test
    @DisplayName("예약 현황 조회 - 없음")
    void get_reservation_list() {
        //when   //then
        assertThrows(ReservationNotFound.class,
            () -> reservationGuestService.findReservationList());
    }

    @Test
    @DisplayName("예약 현황 조회 - 과거 예약 잘 걸러지는지")
    void get_reservation_list_filtering_past_reservation() {
        //given
        Reservation reservationPast = Reservation.builder()
            .guest(guest)
            .host(host)
            .space(space)
            .status(ReservationStatus.COMPLETED)
            .usagePurpose(UsagePurpose.BARISTA_TRAINING)
            .requestMessage("테슽뚜")
            .date(LocalDate.now().minusDays(1))
            .startTime(LocalTime.of(12, 0, 0))
            .endTime(LocalTime.of(16, 0, 0))
            .price(60000)
            .guestCount(2)
            .build();
        reservationRepository.save(reservationPast);
        Reservation reservationFuture = Reservation.builder()
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
        reservationRepository.save(reservationFuture);

        //when
        List<ReservationListResponseDto> result = reservationGuestService.findReservationList();

        //then
        assertThat(result).hasSize(1);
        assertEquals(reservationFuture.getSpace().getName(), result.get(0).getSpaceName());
        assertEquals(reservationFuture.getDate(), result.get(0).getDate());
        assertEquals(reservationFuture.getStartTime(), result.get(0).getStartTime());
        assertEquals(reservationFuture.getEndTime(), result.get(0).getEndTime());
    }

    @Test
    @DisplayName("예약 현황 조회 - 현 시점의 시간 이전의 시간 잘 걸러지는지")
    void get_reservation_list_filtering_past_time_reservation() {
        //given
        int currentTime = LocalTime.now().getHour();
        Reservation reservationPast = Reservation.builder()
            .guest(guest)
            .host(host)
            .space(space)
            .status(ReservationStatus.COMPLETED)
            .usagePurpose(UsagePurpose.BARISTA_TRAINING)
            .requestMessage("테슽뚜")
            .date(LocalDate.now())
            .startTime(LocalTime.of(currentTime - 3, 0, 0))
            .endTime(LocalTime.of(currentTime - 1, 0, 0))
            .price(30000)
            .guestCount(2)
            .build();
        reservationRepository.save(reservationPast);
        Reservation reservationFuture = Reservation.builder()
            .guest(guest)
            .host(host)
            .space(space)
            .status(ReservationStatus.ACCEPTED)
            .usagePurpose(UsagePurpose.BARISTA_TRAINING)
            .requestMessage("테슽뚜")
            .date(LocalDate.now())
            .startTime(LocalTime.of(currentTime + 1, 0, 0))
            .endTime(LocalTime.of(currentTime + 2, 0, 0))
            .price(15000)
            .guestCount(2)
            .build();
        reservationRepository.save(reservationFuture);

        //when
        List<ReservationListResponseDto> result = reservationGuestService.findReservationList();

        //then
        assertThat(result).hasSize(1);
        assertEquals(reservationFuture.getSpace().getName(), result.get(0).getSpaceName());
        assertEquals(reservationFuture.getDate(), result.get(0).getDate());
        assertEquals(reservationFuture.getStartTime(), result.get(0).getStartTime());
        assertEquals(reservationFuture.getEndTime(), result.get(0).getEndTime());
    }

    @Test
    @DisplayName("지난 예약 조회 - 시간 지나면 예약 사용 완료 상태로 변경")
    void get_past_reservation_list_change_status() {
        //given
        Reservation reservationPast = Reservation.builder()
            .guest(guest)
            .host(host)
            .space(space)
            .status(ReservationStatus.ACCEPTED)
            .usagePurpose(UsagePurpose.BARISTA_TRAINING)
            .requestMessage("테슽뚜")
            .date(LocalDate.now().minusDays(1))
            .startTime(LocalTime.of(12, 0, 0))
            .endTime(LocalTime.of(14, 0, 0))
            .price(30000)
            .guestCount(2)
            .build();
        reservationRepository.save(reservationPast);
        Reservation reservationFuture = Reservation.builder()
            .guest(guest)
            .host(host)
            .space(space)
            .status(ReservationStatus.ACCEPTED)
            .usagePurpose(UsagePurpose.BARISTA_TRAINING)
            .requestMessage("테슽뚜")
            .date(LocalDate.now().plusDays(1))
            .startTime(LocalTime.of(15, 0, 0))
            .endTime(LocalTime.of(16, 0, 0))
            .price(15000)
            .guestCount(2)
            .build();
        reservationRepository.save(reservationFuture);

        //when
        List<ReservationListResponseDto> result = reservationGuestService.findPastReservationList();

        //then
        assertThat(result).hasSize(1);
        assertEquals(reservationPast.getSpace().getName(), result.get(0).getSpaceName());
        assertEquals(reservationPast.getDate(), result.get(0).getDate());
        assertEquals(reservationPast.getStartTime(), result.get(0).getStartTime());
        assertEquals(reservationPast.getEndTime(), result.get(0).getEndTime());
        assertEquals(ReservationStatus.COMPLETED, result.get(0).getStatus());
    }

    @Test
    @DisplayName("지난 예약 조회 - 과거 예약 없을 경우")
    void get_past_reservation_list_non_existent_past_reservation() {
        //given
        Reservation reservationFuture = Reservation.builder()
            .guest(guest)
            .host(host)
            .space(space)
            .status(ReservationStatus.ACCEPTED)
            .usagePurpose(UsagePurpose.BARISTA_TRAINING)
            .requestMessage("테슽뚜")
            .date(LocalDate.now().plusDays(1))
            .startTime(LocalTime.of(15, 0, 0))
            .endTime(LocalTime.of(16, 0, 0))
            .price(15000)
            .guestCount(2)
            .build();
        reservationRepository.save(reservationFuture);

        //when  //then
        assertThrows(ReservationNotFound.class,
            () -> reservationGuestService.findPastReservationList());
    }

    @Test
    @DisplayName("예약 취소 - 해당 예약이 존재하지 않을 경우")
    void cancel_reservation_with_not_found_reservation() {
        //when  //then
        assertThrows(ReservationNotFound.class,
            () -> reservationGuestService.cancelReservation(1L));
    }

    @Test
    @DisplayName("예약 취소 - 예약이 확정되었을 경우")
    void cancel_reservation_with_reservation_status_accepted() {
        //given
        Reservation reservationFuture = Reservation.builder()
            .guest(guest)
            .host(host)
            .space(space)
            .status(ReservationStatus.ACCEPTED)
            .usagePurpose(UsagePurpose.BARISTA_TRAINING)
            .requestMessage("테슽뚜")
            .date(LocalDate.now().plusDays(1))
            .startTime(LocalTime.of(15, 0, 0))
            .endTime(LocalTime.of(16, 0, 0))
            .price(15000)
            .guestCount(2)
            .build();
        reservationRepository.save(reservationFuture);

        //when  //then
        assertThrows(MissMatch.class,
            () -> reservationGuestService.cancelReservation(reservationFuture.getId()));
    }

    @Test
    @Transactional
    @DisplayName("예약 취소 - 성공")
    void success_cancel_reservation() {
        //given
        Reservation reservationFuture = Reservation.builder()
            .guest(guest)
            .host(host)
            .space(space)
            .status(ReservationStatus.PENDING)
            .usagePurpose(UsagePurpose.BARISTA_TRAINING)
            .requestMessage("테슽뚜")
            .date(LocalDate.now().plusDays(1))
            .startTime(LocalTime.of(15, 0, 0))
            .endTime(LocalTime.of(16, 0, 0))
            .price(15000)
            .guestCount(2)
            .build();
        reservationRepository.save(reservationFuture);

        //when
        reservationGuestService.cancelReservation(reservationFuture.getId());

        //then
        assertEquals(ReservationStatus.REJECTED, reservationFuture.getStatus());
    }

}