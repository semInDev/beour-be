package com.beour.reservation.host.service;

import static org.junit.jupiter.api.Assertions.*;

import com.beour.global.exception.exceptionType.SpaceNotFoundException;
import com.beour.global.exception.exceptionType.UnauthorityException;
import com.beour.global.exception.exceptionType.UserNotFoundException;
import com.beour.reservation.commons.entity.Reservation;
import com.beour.reservation.commons.enums.ReservationStatus;
import com.beour.reservation.commons.enums.UsagePurpose;
import com.beour.reservation.commons.exceptionType.MissMatch;
import com.beour.reservation.commons.exceptionType.ReservationNotFound;
import com.beour.reservation.commons.repository.ReservationRepository;
import com.beour.reservation.host.dto.CalendarReservationResponseDto;
import com.beour.space.domain.entity.Space;
import com.beour.space.domain.repository.SpaceRepository;
import com.beour.space.domain.repository.TagRepository;
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

@SpringBootTest
@ActiveProfiles("test")
class ReservationCalendarServiceTest {

    @Autowired
    private ReservationCalendarService reservationCalendarService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private SpaceRepository spaceRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    private User guest;
    private User host;
    private User otherHost;
    private Space space;
    private Space otherSpace;
    private Reservation pendingReservation;
    private Reservation acceptedReservation;
    private Reservation rejectedReservation;
    private Reservation deletedReservation;

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

        otherHost = User.builder()
                .loginId("host2")
                .password(passwordEncoder.encode("host2password!"))
                .name("호스트2")
                .nickname("host2")
                .email("host2@gmail.com")
                .phone("01012345679")
                .role("HOST")
                .build();
        userRepository.save(otherHost);

        // 인증 컨텍스트 설정 (host로 로그인)
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                host.getLoginId(), null, Collections.emptyList());
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

        otherSpace = Space.builder()
                .host(otherHost)
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
        spaceRepository.save(otherSpace);

        LocalDate today = LocalDate.now();

        pendingReservation = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space)
                .status(ReservationStatus.PENDING)
                .usagePurpose(UsagePurpose.BARISTA_TRAINING)
                .requestMessage("대기 중인 예약")
                .date(today)
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
                .date(today)
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
                .usagePurpose(UsagePurpose.GROUP_MEETING)
                .requestMessage("거부된 예약")
                .date(today)
                .startTime(LocalTime.of(18, 0, 0))
                .endTime(LocalTime.of(20, 0, 0))
                .price(30000)
                .guestCount(1)
                .build();
        reservationRepository.save(rejectedReservation);

        deletedReservation = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space)
                .status(ReservationStatus.PENDING)
                .usagePurpose(UsagePurpose.OTHER)
                .requestMessage("삭제된 예약")
                .date(today)
                .startTime(LocalTime.of(22, 0, 0))
                .endTime(LocalTime.of(23, 0, 0))
                .price(15000)
                .guestCount(1)
                .build();
        reservationRepository.save(deletedReservation);
        deletedReservation.softDelete();
        reservationRepository.save(deletedReservation);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        tagRepository.deleteAll();
        reservationRepository.deleteAll();
        spaceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("호스트 캘린더 예약 조회 - 전체 예약 조회 (spaceId 없음)")
    void getHostCalendarReservations_without_spaceId() {
        // given
        LocalDate today = LocalDate.now();

        // when
        List<CalendarReservationResponseDto> result = reservationCalendarService.getHostCalendarReservations(today, null);

        // then
        assertEquals(3, result.size()); // 삭제된 예약은 제외
        assertTrue(result.stream().anyMatch(dto -> dto.getStatus() == ReservationStatus.PENDING));
        assertTrue(result.stream().anyMatch(dto -> dto.getStatus() == ReservationStatus.ACCEPTED));
        assertTrue(result.stream().anyMatch(dto -> dto.getStatus() == ReservationStatus.REJECTED));
    }

    @Test
    @DisplayName("호스트 캘린더 예약 조회 - 특정 공간 예약 조회")
    void getHostCalendarReservations_with_spaceId() {
        // given
        LocalDate today = LocalDate.now();

        // when
        List<CalendarReservationResponseDto> result = reservationCalendarService.getHostCalendarReservations(today, space.getId());

        // then
        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(dto -> dto.getSpaceName().equals(space.getName())));
    }

    @Test
    @DisplayName("호스트 캘린더 예약 조회 - 다른 호스트 공간 접근 시도")
    void getHostCalendarReservations_unauthorized_space() {
        // given
        LocalDate today = LocalDate.now();

        // when & then
        assertThrows(UnauthorityException.class, () ->
                reservationCalendarService.getHostCalendarReservations(today, otherSpace.getId()));
    }

    @Test
    @DisplayName("호스트 대기 중인 예약 조회 - 전체 공간")
    void getHostPendingReservations_without_spaceId() {
        // given
        LocalDate today = LocalDate.now();

        // when
        List<CalendarReservationResponseDto> result = reservationCalendarService.getHostPendingReservations(today, null);

        // then
        assertEquals(1, result.size());
        assertEquals(ReservationStatus.PENDING, result.get(0).getStatus());
        assertEquals(LocalTime.of(10, 0, 0), result.get(0).getStartTime());
    }

    @Test
    @DisplayName("호스트 대기 중인 예약 조회 - 특정 공간")
    void getHostPendingReservations_with_spaceId() {
        // given
        LocalDate today = LocalDate.now();

        // when
        List<CalendarReservationResponseDto> result = reservationCalendarService.getHostPendingReservations(today, space.getId());

        // then
        assertEquals(1, result.size());
        assertEquals(ReservationStatus.PENDING, result.get(0).getStatus());
        assertEquals(space.getName(), result.get(0).getSpaceName());
    }

    @Test
    @DisplayName("호스트 승인된 예약 조회 - 전체 공간")
    void getHostAcceptedReservations_without_spaceId() {
        // given
        LocalDate today = LocalDate.now();

        // when
        List<CalendarReservationResponseDto> result = reservationCalendarService.getHostAcceptedReservations(today, null);

        // then
        assertEquals(1, result.size());
        assertEquals(ReservationStatus.ACCEPTED, result.get(0).getStatus());
        assertEquals(LocalTime.of(14, 0, 0), result.get(0).getStartTime());
    }

    @Test
    @DisplayName("호스트 승인된 예약 조회 - 특정 공간")
    void getHostAcceptedReservations_with_spaceId() {
        // given
        LocalDate today = LocalDate.now();

        // when
        List<CalendarReservationResponseDto> result = reservationCalendarService.getHostAcceptedReservations(today, space.getId());

        // then
        assertEquals(1, result.size());
        assertEquals(ReservationStatus.ACCEPTED, result.get(0).getStatus());
        assertEquals(space.getName(), result.get(0).getSpaceName());
    }

    @Test
    @DisplayName("예약 승인 성공")
    void acceptReservation_success() {
        // given
        Long reservationId = pendingReservation.getId();
        Long spaceId = space.getId();

        // when
        reservationCalendarService.acceptReservation(reservationId, spaceId);

        // then
        Reservation updated = reservationRepository.findById(reservationId).get();
        assertEquals(ReservationStatus.ACCEPTED, updated.getStatus());
    }

    @Test
    @DisplayName("예약 승인 - 존재하지 않는 예약")
    void acceptReservation_reservation_not_found() {
        // given
        Long nonExistentReservationId = 999L;
        Long spaceId = space.getId();

        // when & then
        assertThrows(ReservationNotFound.class, () ->
                reservationCalendarService.acceptReservation(nonExistentReservationId, spaceId));
    }

    @Test
    @DisplayName("예약 승인 - 존재하지 않는 공간")
    void acceptReservation_space_not_found() {
        // given
        Long reservationId = pendingReservation.getId();
        Long nonExistentSpaceId = 999L;

        // when & then
        assertThrows(SpaceNotFoundException.class, () ->
                reservationCalendarService.acceptReservation(reservationId, nonExistentSpaceId));
    }

    @Test
    @DisplayName("예약 승인 - 다른 호스트의 공간")
    void acceptReservation_unauthorized_space() {
        // given
        Long reservationId = pendingReservation.getId();
        Long otherSpaceId = otherSpace.getId();

        // when & then
        assertThrows(UnauthorityException.class, () ->
                reservationCalendarService.acceptReservation(reservationId, otherSpaceId));
    }

    @Test
    @DisplayName("예약 승인 - 예약의 공간과 입력된 공간이 다름")
    void acceptReservation_space_mismatch() {
        // given
        // 다른 호스트의 공간에 대한 예약 생성
        Space anotherSpace = Space.builder()
                .host(host)
                .name("다른 공간")
                .spaceCategory(SpaceCategory.COOKING)
                .useCategory(UseCategory.COOKING)
                .maxCapacity(2)
                .address("서울시 강남구")
                .detailAddress("다른 건물")
                .pricePerHour(10000)
                .thumbnailUrl("https://example.img")
                .latitude(123.12)
                .longitude(123.12)
                .avgRating(0.0)
                .availableTimes(new ArrayList<>())
                .build();
        spaceRepository.save(anotherSpace);

        Long reservationId = pendingReservation.getId();
        Long differentSpaceId = anotherSpace.getId();

        // when & then
        assertThrows(MissMatch.class, () ->
                reservationCalendarService.acceptReservation(reservationId, differentSpaceId));
    }

    @Test
    @DisplayName("예약 승인 - 다른 호스트의 예약")
    void acceptReservation_unauthorized_host() {
        // given
        // 다른 호스트로 인증 컨텍스트 변경
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                otherHost.getLoginId(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        Long reservationId = pendingReservation.getId();
        Long spaceId = space.getId();

        // when & then
        assertThrows(UnauthorityException.class, () ->
                reservationCalendarService.acceptReservation(reservationId, spaceId));
    }

    @Test
    @DisplayName("예약 거부 성공")
    void rejectReservation_success() {
        // given
        Long reservationId = pendingReservation.getId();
        Long spaceId = space.getId();

        // when
        reservationCalendarService.rejectReservation(reservationId, spaceId);

        // then
        Reservation updated = reservationRepository.findById(reservationId).get();
        assertEquals(ReservationStatus.REJECTED, updated.getStatus());
    }

    @Test
    @DisplayName("예약 거부 - 존재하지 않는 예약")
    void rejectReservation_reservation_not_found() {
        // given
        Long nonExistentReservationId = 999L;
        Long spaceId = space.getId();

        // when & then
        assertThrows(ReservationNotFound.class, () ->
                reservationCalendarService.rejectReservation(nonExistentReservationId, spaceId));
    }

    @Test
    @DisplayName("예약 거부 - 존재하지 않는 공간")
    void rejectReservation_space_not_found() {
        // given
        Long reservationId = pendingReservation.getId();
        Long nonExistentSpaceId = 999L;

        // when & then
        assertThrows(SpaceNotFoundException.class, () ->
                reservationCalendarService.rejectReservation(reservationId, nonExistentSpaceId));
    }

    @Test
    @DisplayName("토큰에서 사용자 조회 - 존재하지 않는 사용자")
    void findUserFromToken_user_not_found() {
        // given
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "nonexistent", null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // when & then
        assertThrows(UserNotFoundException.class, () ->
                reservationCalendarService.getHostCalendarReservations(LocalDate.now(), null));
    }

    @Test
    @DisplayName("예약 날짜가 다른 경우 조회되지 않음")
    void getHostCalendarReservations_different_date() {
        // given
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        // when
        List<CalendarReservationResponseDto> result = reservationCalendarService.getHostCalendarReservations(tomorrow, null);

        // then
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("삭제된 예약은 조회되지 않음")
    void getHostCalendarReservations_deleted_reservation_excluded() {
        // given
        LocalDate today = LocalDate.now();

        // 모든 예약을 삭제 처리
        pendingReservation.softDelete();
        acceptedReservation.softDelete();
        rejectedReservation.softDelete();
        reservationRepository.save(pendingReservation);
        reservationRepository.save(acceptedReservation);
        reservationRepository.save(rejectedReservation);

        // when
        List<CalendarReservationResponseDto> result = reservationCalendarService.getHostCalendarReservations(today, null);

        // then
        assertEquals(0, result.size());
    }
}
