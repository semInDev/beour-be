package com.beour.reservation.guest.service;

import static org.junit.jupiter.api.Assertions.*;

import com.beour.reservation.commons.exceptionType.AvailableTimeNotFound;
import com.beour.reservation.guest.dto.CheckAvailableTimesRequestDto;
import com.beour.space.domain.entity.AvailableTime;
import com.beour.space.domain.entity.Space;
import com.beour.space.domain.repository.AvailableTimeRepository;
import com.beour.space.domain.repository.SpaceRepository;
import com.beour.space.domain.repository.TagRepository;
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
        spaceRepository.deleteAll();
        userRepository.deleteAll();
    }


    /**
     * #73
     * 예약 가능한 시간 조회
     *  - 과거 날짜일 경우
     *  - 예약 거절된 상태인 시간 조회 가능한지
     *  - 오늘날짜로 조회할 경우 현재 이후의 시간만 조회 가능한지
     *  - 예약 날짜 시간을 제외하고 조회가 가능한지
     */

    @Test
    @DisplayName("시간, 날짜 유효성 조회 - 과거 날짜로 조회")
    void past_date_checkReservationAvailableDateAndGetAvailableTime(){
        //given
        CheckAvailableTimesRequestDto requestDto = new CheckAvailableTimesRequestDto(space.getId(), LocalDate.now().minusDays(1));

        //when  //then
        assertThrows(AvailableTimeNotFound.class, () -> checkAvailableTimeService.checkReservationAvailableDateAndGetAvailableTime(requestDto));
    }

    @Test
    @DisplayName("시간, 날짜 유효성 조회 - 가능한 시간 없을 경우")
    void non_existent_available_time_checkReservationAvailableDateAndGetAvailableTime(){
        //given
        CheckAvailableTimesRequestDto requestDto = new CheckAvailableTimesRequestDto(space.getId(), LocalDate.now().plusDays(2));

        //when  //then
        assertThrows(AvailableTimeNotFound.class, () -> checkAvailableTimeService.checkReservationAvailableDateAndGetAvailableTime(requestDto));
    }

    @Test
    @DisplayName("시간, 날짜 유효성 조회 - 시간 있을 경우")
    void exist_available_time_checkReservationAvailableDateAndGetAvailableTime(){
        //given
        CheckAvailableTimesRequestDto requestDto = new CheckAvailableTimesRequestDto(space.getId(), LocalDate.now().plusDays(1));

        //when
        AvailableTime availableTime = checkAvailableTimeService.checkReservationAvailableDateAndGetAvailableTime(requestDto);

        //then
        assertEquals(availableTimeNext.getSpace().getId(), availableTime.getSpace().getId());
        assertEquals(availableTimeNext.getDate(), availableTime.getDate());
        assertEquals(availableTimeNext.getStartTime(), availableTime.getStartTime());
        assertEquals(availableTimeNext.getEndTime(), availableTime.getEndTime());
    }


}