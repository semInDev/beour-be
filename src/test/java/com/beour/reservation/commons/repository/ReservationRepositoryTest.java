package com.beour.reservation.commons.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.beour.reservation.commons.entity.Reservation;
import com.beour.reservation.commons.enums.ReservationStatus;
import com.beour.reservation.commons.enums.UsagePurpose;
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
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
class ReservationRepositoryTest {

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
    @Transactional
    void findBySpaceIdAndDateAndDeletedAtIsNull_test() {
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

        Reservation reservation2 = Reservation.builder()
            .guest(guest)
            .host(host)
            .space(space)
            .status(ReservationStatus.ACCEPTED)
            .usagePurpose(UsagePurpose.BARISTA_TRAINING)
            .requestMessage("테슽뚜")
            .date(LocalDate.now().plusDays(1))
            .startTime(LocalTime.of(16, 0, 0))
            .endTime(LocalTime.of(17, 0, 0))
            .price(15000)
            .guestCount(2)
            .build();
        reservationRepository.save(reservation2);
        reservation2.softDelete();

        //when
        List<Reservation> result = reservationRepository.findBySpaceIdAndDateAndDeletedAtIsNull(
            space.getId(), LocalDate.now().plusDays(1));

        //then
        assertEquals(1, result.size());
        assertEquals(reservation.getSpace(), result.get(0).getSpace());
        assertEquals(reservation.getStartTime(), result.get(0).getStartTime());
        assertEquals(reservation.getEndTime(), result.get(0).getEndTime());
    }

    @Test
    void findBySpaceIdAndDateAndStatusNot_test() {
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

        Reservation reservation2 = Reservation.builder()
            .guest(guest)
            .host(host)
            .space(space)
            .status(ReservationStatus.REJECTED)
            .usagePurpose(UsagePurpose.BARISTA_TRAINING)
            .requestMessage("테슽뚜")
            .date(LocalDate.now().plusDays(1))
            .startTime(LocalTime.of(16, 0, 0))
            .endTime(LocalTime.of(17, 0, 0))
            .price(15000)
            .guestCount(2)
            .build();
        reservationRepository.save(reservation2);

        //when
        List<Reservation> result = reservationRepository.findBySpaceIdAndDateAndStatusNot(
            space.getId(), LocalDate.now().plusDays(1), ReservationStatus.REJECTED);

        //then
        assertEquals(1, result.size());
        assertEquals(reservation.getStartTime(), result.get(0).getStartTime());
        assertEquals(reservation.getEndTime(), result.get(0).getEndTime());
        assertEquals(reservation.getStatus(), result.get(0).getStatus());
    }

    @Test
    void findUpcomingReservationsByGuest_test() {
        //given
        int currentTime = LocalTime.now().getHour();
        Reservation reservation = Reservation.builder()
            .guest(guest)
            .host(host)
            .space(space)
            .status(ReservationStatus.ACCEPTED)
            .usagePurpose(UsagePurpose.BARISTA_TRAINING)
            .requestMessage("테슽뚜")
            .date(LocalDate.now())
            .startTime(LocalTime.of(currentTime - 1, 0, 0))
            .endTime(LocalTime.of(currentTime + 1, 0, 0))
            .price(30000)
            .guestCount(2)
            .build();
        reservationRepository.save(reservation);

        Reservation reservation2 = Reservation.builder()
            .guest(guest)
            .host(host)
            .space(space)
            .status(ReservationStatus.REJECTED)
            .usagePurpose(UsagePurpose.BARISTA_TRAINING)
            .requestMessage("테슽뚜")
            .date(LocalDate.now())
            .startTime(LocalTime.of(currentTime + 1, 0, 0))
            .endTime(LocalTime.of(currentTime + 2, 0, 0))
            .price(15000)
            .guestCount(2)
            .build();
        reservationRepository.save(reservation2);

        //when
        List<Reservation> result = reservationRepository.findUpcomingReservationsByGuest(
            guest.getId(), LocalDate.now(), LocalTime.now());

        //then
        assertEquals(1, result.size());
        assertEquals(reservation2.getDate(), result.get(0).getDate());
        assertEquals(reservation2.getStartTime(), result.get(0).getStartTime());
        assertEquals(reservation2.getEndTime(), result.get(0).getEndTime());
        assertEquals(reservation2.getStatus(), result.get(0).getStatus());
    }

    @Test
    void findPastReservationsByGuest_test() {
        //given
        int currentTime = LocalTime.now().getHour();
        Reservation reservation = Reservation.builder()
            .guest(guest)
            .host(host)
            .space(space)
            .status(ReservationStatus.ACCEPTED)
            .usagePurpose(UsagePurpose.BARISTA_TRAINING)
            .requestMessage("테슽뚜")
            .date(LocalDate.now())
            .startTime(LocalTime.of(currentTime - 3, 0, 0))
            .endTime(LocalTime.of(currentTime - 1, 0, 0))
            .price(30000)
            .guestCount(2)
            .build();
        reservationRepository.save(reservation);

        Reservation reservation2 = Reservation.builder()
            .guest(guest)
            .host(host)
            .space(space)
            .status(ReservationStatus.REJECTED)
            .usagePurpose(UsagePurpose.BARISTA_TRAINING)
            .requestMessage("테슽뚜")
            .date(LocalDate.now())
            .startTime(LocalTime.of(currentTime + 1, 0, 0))
            .endTime(LocalTime.of(currentTime + 2, 0, 0))
            .price(15000)
            .guestCount(2)
            .build();
        reservationRepository.save(reservation2);

        //when
        List<Reservation> result = reservationRepository.findPastReservationsByGuest(
            guest.getId(), LocalDate.now(), LocalTime.now());

        //then
        assertEquals(1, result.size());
        assertEquals(reservation.getDate(), result.get(0).getDate());
        assertEquals(reservation.getStartTime(), result.get(0).getStartTime());
        assertEquals(reservation.getEndTime(), result.get(0).getEndTime());
        assertEquals(reservation.getStatus(), result.get(0).getStatus());
    }

}