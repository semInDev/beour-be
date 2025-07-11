package com.beour.reservation.host.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.beour.global.exception.exceptionType.SpaceNotFoundException;
import com.beour.global.exception.exceptionType.UnauthorityException;
import com.beour.reservation.commons.entity.Reservation;
import com.beour.reservation.commons.enums.ReservationStatus;
import com.beour.reservation.commons.enums.UsagePurpose;
import com.beour.reservation.commons.exceptionType.ReservationNotFound;
import com.beour.reservation.commons.repository.ReservationRepository;
import com.beour.reservation.host.dto.HostReservationListResponseDto;
import com.beour.reservation.host.dto.HostSpaceListResponseDto;
import com.beour.space.domain.entity.Space;
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
class ReservationHostServiceTest {

    @Autowired
    private ReservationHostService reservationHostService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private SpaceRepository spaceRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    private User guest;
    private User host;
    private User anotherHost;
    private Space space1;
    private Space space2;
    private Space anotherHostSpace;

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
                .phone("01098765432")
                .role("HOST")
                .build();
        userRepository.save(anotherHost);

        // 호스트로 인증 설정
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                host.getLoginId(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

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
                .availableTimes(new ArrayList<>())
                .build();
        spaceRepository.save(space1);

        space2 = Space.builder()
                .host(host)
                .name("공간2")
                .spaceCategory(SpaceCategory.CAFE)
                .useCategory(UseCategory.MEETING)
                .maxCapacity(5)
                .address("서울시 서초구")
                .detailAddress("빌딩 3층")
                .pricePerHour(20000)
                .thumbnailUrl("https://example2.img")
                .latitude(124.12)
                .longitude(124.12)
                .avgRating(0.0)
                .availableTimes(new ArrayList<>())
                .build();
        spaceRepository.save(space2);

        anotherHostSpace = Space.builder()
                .host(anotherHost)
                .name("다른 호스트 공간")
                .spaceCategory(SpaceCategory.CAFE)
                .useCategory(UseCategory.MEETING)
                .maxCapacity(10)
                .address("서울시 마포구")
                .detailAddress("빌딩 1층")
                .pricePerHour(25000)
                .thumbnailUrl("https://example3.img")
                .latitude(125.12)
                .longitude(125.12)
                .avgRating(0.0)
                .availableTimes(new ArrayList<>())
                .build();
        spaceRepository.save(anotherHostSpace);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        reservationRepository.deleteAll();
        spaceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("호스트 공간 목록 조회 - 성공")
    void get_host_spaces_success() {
        //when
        List<HostSpaceListResponseDto> result = reservationHostService.getHostSpaces();

        //then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getSpaceName()).isEqualTo("공간1");
        assertThat(result.get(1).getSpaceName()).isEqualTo("공간2");
    }

    @Test
    @DisplayName("호스트 공간 목록 조회 - 공간이 없을 경우")
    void get_host_spaces_empty() {
        //given
        // 공간을 모두 삭제
        spaceRepository.deleteAll();

        //when & then
        assertThrows(SpaceNotFoundException.class, () -> reservationHostService.getHostSpaces());
    }

    @Test
    @Transactional
    @DisplayName("날짜별 호스트 예약 조회 - 성공")
    void get_host_reservations_by_date_success() {
        //given
        LocalDate targetDate = LocalDate.now().plusDays(1);

        Reservation acceptedReservation = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space1)
                .status(ReservationStatus.ACCEPTED)
                .usagePurpose(UsagePurpose.BARISTA_TRAINING)
                .requestMessage("테스트 메시지")
                .date(targetDate)
                .startTime(LocalTime.of(14, 0, 0))
                .endTime(LocalTime.of(16, 0, 0))
                .price(30000)
                .guestCount(2)
                .build();
        reservationRepository.save(acceptedReservation);

        Reservation pendingReservation = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space2)
                .status(ReservationStatus.PENDING)
                .usagePurpose(UsagePurpose.COOKING_PRACTICE)
                .requestMessage("대기 중 예약")
                .date(targetDate)
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(12, 0, 0))
                .price(40000)
                .guestCount(3)
                .build();
        reservationRepository.save(pendingReservation);

        //when
        List<HostReservationListResponseDto> result = reservationHostService.getHostReservationsByDate(targetDate);

        //then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getReservationId()).isEqualTo(acceptedReservation.getId());
        assertThat(result.get(0).getGuestName()).isEqualTo("게스트");
        assertThat(result.get(0).getStatus()).isEqualTo(ReservationStatus.ACCEPTED);
        assertThat(result.get(0).getSpaceName()).isEqualTo("공간1");
        assertThat(result.get(0).getStartTime()).isEqualTo(LocalTime.of(14, 0, 0));
        assertThat(result.get(0).getEndTime()).isEqualTo(LocalTime.of(16, 0, 0));
        assertThat(result.get(0).getGuestCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("날짜별 호스트 예약 조회 - 확정된 예약이 없을 경우")
    void get_host_reservations_by_date_no_accepted_reservations() {
        //given
        LocalDate targetDate = LocalDate.now().plusDays(1);

        Reservation pendingReservation = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space1)
                .status(ReservationStatus.PENDING)
                .usagePurpose(UsagePurpose.BARISTA_TRAINING)
                .requestMessage("대기 중")
                .date(targetDate)
                .startTime(LocalTime.of(14, 0, 0))
                .endTime(LocalTime.of(16, 0, 0))
                .price(30000)
                .guestCount(2)
                .build();
        reservationRepository.save(pendingReservation);

        //when & then
        assertThrows(ReservationNotFound.class,
                () -> reservationHostService.getHostReservationsByDate(targetDate));
    }

    @Test
    @DisplayName("날짜별 호스트 예약 조회 - 예약이 전혀 없을 경우")
    void get_host_reservations_by_date_no_reservations() {
        //given
        LocalDate targetDate = LocalDate.now().plusDays(1);

        //when & then
        assertThrows(ReservationNotFound.class,
                () -> reservationHostService.getHostReservationsByDate(targetDate));
    }

    @Test
    @Transactional
    @DisplayName("날짜와 공간별 호스트 예약 조회 - 성공")
    void get_host_reservations_by_date_and_space_success() {
        //given
        LocalDate targetDate = LocalDate.now().plusDays(1);

        Reservation acceptedReservation1 = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space1)
                .status(ReservationStatus.ACCEPTED)
                .usagePurpose(UsagePurpose.BARISTA_TRAINING)
                .requestMessage("공간1 예약")
                .date(targetDate)
                .startTime(LocalTime.of(14, 0, 0))
                .endTime(LocalTime.of(16, 0, 0))
                .price(30000)
                .guestCount(2)
                .build();
        reservationRepository.save(acceptedReservation1);

        Reservation acceptedReservation2 = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space2)
                .status(ReservationStatus.ACCEPTED)
                .usagePurpose(UsagePurpose.COOKING_PRACTICE)
                .requestMessage("공간2 예약")
                .date(targetDate)
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(12, 0, 0))
                .price(40000)
                .guestCount(3)
                .build();
        reservationRepository.save(acceptedReservation2);

        //when
        List<HostReservationListResponseDto> result = reservationHostService.getHostReservationsByDateAndSpace(
                targetDate, space1.getId());

        //then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getReservationId()).isEqualTo(acceptedReservation1.getId());
        assertThat(result.get(0).getSpaceName()).isEqualTo("공간1");
        assertThat(result.get(0).getGuestCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("날짜와 공간별 호스트 예약 조회 - 존재하지 않는 공간")
    void get_host_reservations_by_date_and_space_space_not_found() {
        //given
        LocalDate targetDate = LocalDate.now().plusDays(1);
        Long nonExistentSpaceId = 999L;

        //when & then
        assertThrows(SpaceNotFoundException.class,
                () -> reservationHostService.getHostReservationsByDateAndSpace(targetDate, nonExistentSpaceId));
    }

    @Test
    @DisplayName("날짜와 공간별 호스트 예약 조회 - 다른 호스트의 공간")
    void get_host_reservations_by_date_and_space_not_owner() {
        //given
        LocalDate targetDate = LocalDate.now().plusDays(1);

        //when & then
        assertThrows(UnauthorityException.class,
                () -> reservationHostService.getHostReservationsByDateAndSpace(targetDate, anotherHostSpace.getId()));
    }

    @Test
    @DisplayName("날짜와 공간별 호스트 예약 조회 - 확정된 예약이 없을 경우")
    void get_host_reservations_by_date_and_space_no_accepted_reservations() {
        //given
        LocalDate targetDate = LocalDate.now().plusDays(1);

        Reservation pendingReservation = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space1)
                .status(ReservationStatus.PENDING)
                .usagePurpose(UsagePurpose.BARISTA_TRAINING)
                .requestMessage("대기 중")
                .date(targetDate)
                .startTime(LocalTime.of(14, 0, 0))
                .endTime(LocalTime.of(16, 0, 0))
                .price(30000)
                .guestCount(2)
                .build();
        reservationRepository.save(pendingReservation);

        //when & then
        assertThrows(ReservationNotFound.class,
                () -> reservationHostService.getHostReservationsByDateAndSpace(targetDate, space1.getId()));
    }

    @Test
    @Transactional
    @DisplayName("현재 사용 중인 예약 판별 - 현재 시간이 예약 시간 내에 있는 경우")
    void is_currently_in_use_true() {
        //given
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // 현재 시간 기준으로 1시간 전부터 1시간 후까지의 예약 생성
        Reservation currentReservation = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space1)
                .status(ReservationStatus.ACCEPTED)
                .usagePurpose(UsagePurpose.BARISTA_TRAINING)
                .requestMessage("현재 사용 중")
                .date(today)
                .startTime(now.minusHours(1))
                .endTime(now.plusHours(1))
                .price(30000)
                .guestCount(2)
                .build();
        reservationRepository.save(currentReservation);

        //when
        List<HostReservationListResponseDto> result = reservationHostService.getHostReservationsByDate(today);

        //then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).isCurrentlyInUse()).isTrue();
    }

    @Test
    @Transactional
    @DisplayName("현재 사용 중인 예약 판별 - 예약 시간이 아직 시작되지 않은 경우")
    void is_currently_in_use_false_future() {
        //given
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // 현재 시간 기준으로 1시간 후부터 2시간 후까지의 예약 생성
        Reservation futureReservation = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space1)
                .status(ReservationStatus.ACCEPTED)
                .usagePurpose(UsagePurpose.BARISTA_TRAINING)
                .requestMessage("미래 예약")
                .date(today)
                .startTime(now.plusHours(1))
                .endTime(now.plusHours(2))
                .price(15000)
                .guestCount(2)
                .build();
        reservationRepository.save(futureReservation);

        //when
        List<HostReservationListResponseDto> result = reservationHostService.getHostReservationsByDate(today);

        //then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).isCurrentlyInUse()).isFalse();
    }

    @Test
    @Transactional
    @DisplayName("현재 사용 중인 예약 판별 - 예약 시간이 이미 종료된 경우")
    void is_currently_in_use_false_past() {
        //given
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // 현재 시간 기준으로 2시간 전부터 1시간 전까지의 예약 생성
        Reservation pastReservation = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space1)
                .status(ReservationStatus.ACCEPTED)
                .usagePurpose(UsagePurpose.BARISTA_TRAINING)
                .requestMessage("과거 예약")
                .date(today)
                .startTime(now.minusHours(2))
                .endTime(now.minusHours(1))
                .price(15000)
                .guestCount(2)
                .build();
        reservationRepository.save(pastReservation);

        //when
        List<HostReservationListResponseDto> result = reservationHostService.getHostReservationsByDate(today);

        //then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).isCurrentlyInUse()).isFalse();
    }

    @Test
    @Transactional
    @DisplayName("다양한 예약 상태 필터링 - ACCEPTED 상태만 조회")
    void filter_only_accepted_reservations() {
        //given
        LocalDate targetDate = LocalDate.now().plusDays(1);

        Reservation acceptedReservation = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space1)
                .status(ReservationStatus.ACCEPTED)
                .usagePurpose(UsagePurpose.BARISTA_TRAINING)
                .requestMessage("승인된 예약")
                .date(targetDate)
                .startTime(LocalTime.of(14, 0, 0))
                .endTime(LocalTime.of(16, 0, 0))
                .price(30000)
                .guestCount(2)
                .build();
        reservationRepository.save(acceptedReservation);

        Reservation pendingReservation = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space1)
                .status(ReservationStatus.PENDING)
                .usagePurpose(UsagePurpose.COOKING_PRACTICE)
                .requestMessage("대기 중 예약")
                .date(targetDate)
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(12, 0, 0))
                .price(30000)
                .guestCount(2)
                .build();
        reservationRepository.save(pendingReservation);

        Reservation rejectedReservation = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space1)
                .status(ReservationStatus.REJECTED)
                .usagePurpose(UsagePurpose.FILMING)
                .requestMessage("거부된 예약")
                .date(targetDate)
                .startTime(LocalTime.of(18, 0, 0))
                .endTime(LocalTime.of(20, 0, 0))
                .price(30000)
                .guestCount(2)
                .build();
        reservationRepository.save(rejectedReservation);

        Reservation completedReservation = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space1)
                .status(ReservationStatus.COMPLETED)
                .usagePurpose(UsagePurpose.GROUP_MEETING)
                .requestMessage("완료된 예약")
                .date(targetDate)
                .startTime(LocalTime.of(8, 0, 0))
                .endTime(LocalTime.of(10, 0, 0))
                .price(30000)
                .guestCount(2)
                .build();
        reservationRepository.save(completedReservation);

        //when
        List<HostReservationListResponseDto> result = reservationHostService.getHostReservationsByDate(targetDate);

        //then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getReservationId()).isEqualTo(acceptedReservation.getId());
        assertThat(result.get(0).getStatus()).isEqualTo(ReservationStatus.ACCEPTED);
    }
}
