package com.beour.reservation.guest.service;

import static org.junit.jupiter.api.Assertions.*;

import com.beour.reservation.commons.entity.Reservation;
import com.beour.reservation.commons.enums.ReservationStatus;
import com.beour.reservation.commons.enums.UsagePurpose;
import com.beour.global.exception.exceptionType.AvailableTimeNotFound;
import com.beour.reservation.commons.repository.ReservationRepository;
import com.beour.reservation.guest.dto.SpaceAvailableTimeResponseDto;
import com.beour.space.domain.entity.AvailableTime;
import com.beour.space.domain.entity.Space;
import com.beour.space.domain.repository.AvailableTimeRepository;
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
class CheckAvailableTimeServiceTest {

    @Autowired
    private CheckAvailableTimeService checkAvailableTimeService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private SpaceRepository spaceRepository;
    @Autowired
    private TagRepository tagRepository;
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
        tagRepository.deleteAll();
        availableTimeRepository.deleteAll();
        reservationRepository.deleteAll();
        spaceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("시간, 날짜 유효성 조회 - 과거 날짜로 조회")
    void past_date_checkReservationAvailableDateAndGetAvailableTime(){
        //when  //then
        assertThrows(AvailableTimeNotFound.class, () -> checkAvailableTimeService.checkReservationAvailableDateAndGetAvailableTime(space.getId(), LocalDate.now().minusDays(1)));
    }

    @Test
    @DisplayName("시간, 날짜 유효성 조회 - 가능한 시간 없을 경우")
    void non_existent_available_time_checkReservationAvailableDateAndGetAvailableTime(){
        //when  //then
        assertThrows(AvailableTimeNotFound.class, () -> checkAvailableTimeService.checkReservationAvailableDateAndGetAvailableTime(space.getId(), LocalDate.now().plusDays(2)));
    }

    @Test
    @DisplayName("시간, 날짜 유효성 조회 - 시간 있을 경우")
    void exist_available_time_checkReservationAvailableDateAndGetAvailableTime(){
        //when
        AvailableTime availableTime = checkAvailableTimeService.checkReservationAvailableDateAndGetAvailableTime(space.getId(), LocalDate.now().plusDays(1));

        //then
        assertEquals(availableTimeNext.getSpace().getId(), availableTime.getSpace().getId());
        assertEquals(availableTimeNext.getDate(), availableTime.getDate());
        assertEquals(availableTimeNext.getStartTime(), availableTime.getStartTime());
        assertEquals(availableTimeNext.getEndTime(), availableTime.getEndTime());
    }

    @Test
    @DisplayName("예약 가능한 시간 조회 - 과거 날짜로 조회")
    void past_date_findAvailableTime(){
        //when  //then
        assertThrows(AvailableTimeNotFound.class, () -> checkAvailableTimeService.findAvailableTime(
            space.getId(), LocalDate.now().minusDays(1)));
    }

    @Test
    @DisplayName("예약 가능한 시간 조회 - 가능한 시간 없을 경우")
    void non_existent_available_time_findAvailableTime(){
        //when  //then
        assertThrows(AvailableTimeNotFound.class, () -> checkAvailableTimeService.findAvailableTime(space.getId(), LocalDate.now().plusDays(2)));
    }

    @Test
    @DisplayName("예약 가능한 시간 조회 - 오늘 날짜일 경우 현재 이후의 시간만 조회")
    void get_available_time_after_current(){
        //given
        List<LocalTime> expectedHours = new ArrayList<>();
        for(int i = LocalTime.now().getHour() + 1; i < 23; i++){
            expectedHours.add(LocalTime.of(i, 0, 0));
        }

        //when
        SpaceAvailableTimeResponseDto result = checkAvailableTimeService.findAvailableTime(space.getId(), LocalDate.now());

        //then
        assertEquals(expectedHours.size(), result.getTimeList().size());
        assertIterableEquals(expectedHours, result.getTimeList());
    }

    @Test
    @DisplayName("예약 가능한 시간 조회 - 예약있는 시간 제외한 예약 가능 시간 조회")
    void get_availabe_time_except_reservation(){
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
        List<LocalTime> expectedHours = new ArrayList<>();
        for(int i = 1; i < 12; i++){
            expectedHours.add(LocalTime.of(i, 0, 0));
        }
        for(int i = 16; i < 23; i++){
            expectedHours.add(LocalTime.of(i, 0, 0));
        }


        //when
        SpaceAvailableTimeResponseDto result = checkAvailableTimeService.findAvailableTime(space.getId(), LocalDate.now().plusDays(1));

        //then
        assertEquals(expectedHours.size(), result.getTimeList().size());
        assertIterableEquals(expectedHours, result.getTimeList());
    }

    @Test
    @DisplayName("예약 가능한 시간 조회 - 예약 거절된 시간 포함해 예약 가능 시간 조회")
    void get_availabe_time_include_rejected_reservation(){
        //given
        Reservation reservation = Reservation.builder()
            .guest(guest)
            .host(host)
            .space(space)
            .status(ReservationStatus.REJECTED)
            .usagePurpose(UsagePurpose.BARISTA_TRAINING)
            .requestMessage("테슽뚜")
            .date(LocalDate.now().plusDays(1))
            .startTime(LocalTime.of(12, 0, 0))
            .endTime(LocalTime.of(16, 0, 0))
            .price(60000)
            .guestCount(2)
            .build();
        reservationRepository.save(reservation);
        List<LocalTime> expectedHours = new ArrayList<>();
        for(int i = 1; i < 23; i++){
            expectedHours.add(LocalTime.of(i, 0, 0));
        }

        //when
        SpaceAvailableTimeResponseDto result = checkAvailableTimeService.findAvailableTime(space.getId(), LocalDate.now().plusDays(1));

        //then
        assertEquals(expectedHours.size(), result.getTimeList().size());
        assertIterableEquals(expectedHours, result.getTimeList());
    }
}