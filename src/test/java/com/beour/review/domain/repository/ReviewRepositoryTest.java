package com.beour.review.domain.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.beour.reservation.commons.entity.Reservation;
import com.beour.reservation.commons.enums.ReservationStatus;
import com.beour.reservation.commons.enums.UsagePurpose;
import com.beour.reservation.commons.repository.ReservationRepository;
import com.beour.review.domain.entity.Review;
import com.beour.space.domain.entity.Space;
import com.beour.space.domain.repository.SpaceRepository;
import com.beour.space.domain.enums.SpaceCategory;
import com.beour.space.domain.enums.UseCategory;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ReviewRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private SpaceRepository spaceRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReviewRepository reviewRepository;

    private User guest;
    private User host;
    private Space space;
    private Reservation reservation;

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

        reservation = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space)
                .status(ReservationStatus.ACCEPTED)
                .usagePurpose(UsagePurpose.BARISTA_TRAINING)
                .requestMessage("테스트")
                .date(LocalDate.now().minusDays(1))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(12, 0, 0))
                .price(30000)
                .guestCount(2)
                .build();
        reservationRepository.save(reservation);
    }

    @AfterEach
    void tearDown() {
        reviewRepository.deleteAll();
        reservationRepository.deleteAll();
        spaceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("게스트ID, 공간ID, 예약날짜로 리뷰 찾기 - 삭제되지 않은 리뷰")
    void findByGuestIdAndSpaceIdAndReservedDateAndDeletedAtIsNull_success() {
        //given
        Review review = Review.builder()
                .guest(guest)
                .space(space)
                .reservation(reservation)
                .rating(5)
                .content("정말 좋은 공간이었습니다!")
                .reservedDate(LocalDate.now().minusDays(1))
                .build();
        reviewRepository.save(review);

        //when
        Optional<Review> result = reviewRepository.findByGuestIdAndSpaceIdAndReservedDateAndDeletedAtIsNull(
                guest.getId(), space.getId(), LocalDate.now().minusDays(1));

        //then
        assertTrue(result.isPresent());
        assertEquals(review.getId(), result.get().getId());
        assertEquals(review.getGuest().getId(), result.get().getGuest().getId());
        assertEquals(review.getSpace().getId(), result.get().getSpace().getId());
        assertEquals(review.getReservedDate(), result.get().getReservedDate());
    }

    @Test
    @DisplayName("게스트ID, 공간ID, 예약날짜로 리뷰 찾기 - 삭제된 리뷰는 제외")
    void findByGuestIdAndSpaceIdAndReservedDateAndDeletedAtIsNull_excludeDeleted() {
        //given
        Review review = Review.builder()
                .guest(guest)
                .space(space)
                .reservation(reservation)
                .rating(5)
                .content("정말 좋은 공간이었습니다!")
                .reservedDate(LocalDate.now().minusDays(1))
                .build();
        reviewRepository.save(review);
        review.softDelete();
        reviewRepository.save(review);

        //when
        Optional<Review> result = reviewRepository.findByGuestIdAndSpaceIdAndReservedDateAndDeletedAtIsNull(
                guest.getId(), space.getId(), LocalDate.now().minusDays(1));

        //then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("공간ID로 리뷰 수 조회 - 삭제되지 않은 리뷰만 카운트")
    void countBySpaceIdAndDeletedAtIsNull_test() {
        //given
        Reservation reservation1 = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space)
                .status(ReservationStatus.ACCEPTED)
                .usagePurpose(UsagePurpose.BARISTA_TRAINING)
                .requestMessage("테스트")
                .date(LocalDate.now().minusDays(1))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(12, 0, 0))
                .price(30000)
                .guestCount(2)
                .build();
        reservationRepository.save(reservation1);

        Review review1 = Review.builder()
                .guest(guest)
                .space(space)
                .reservation(reservation1)
                .rating(5)
                .content("좋은 공간입니다")
                .reservedDate(LocalDate.now().minusDays(1))
                .build();
        reviewRepository.save(review1);

        Reservation reservation2 = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space)
                .status(ReservationStatus.ACCEPTED)
                .usagePurpose(UsagePurpose.BARISTA_TRAINING)
                .requestMessage("테스트")
                .date(LocalDate.now().minusDays(1))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(12, 0, 0))
                .price(30000)
                .guestCount(2)
                .build();
        reservationRepository.save(reservation2);

        Review review2 = Review.builder()
                .guest(guest)
                .space(space)
                .reservation(reservation2)
                .rating(4)
                .content("괜찮은 공간입니다")
                .reservedDate(LocalDate.now().minusDays(2))
                .build();
        reviewRepository.save(review2);

        Reservation reservation3 = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space)
                .status(ReservationStatus.ACCEPTED)
                .usagePurpose(UsagePurpose.BARISTA_TRAINING)
                .requestMessage("테스트")
                .date(LocalDate.now().minusDays(1))
                .startTime(LocalTime.of(10, 0, 0))
                .endTime(LocalTime.of(12, 0, 0))
                .price(30000)
                .guestCount(2)
                .build();
        reservationRepository.save(reservation3);

        Review review3 = Review.builder()
                .guest(guest)
                .space(space)
                .reservation(reservation3)
                .rating(3)
                .content("삭제될 리뷰")
                .reservedDate(LocalDate.now().minusDays(3))
                .build();
        reviewRepository.save(review3);
        review3.softDelete();
        reviewRepository.save(review3);

        //when
        long count = reviewRepository.countBySpaceIdAndDeletedAtIsNull(space.getId());

        //then
        assertEquals(2, count);
    }

    @Test
    @DisplayName("최근 5개 리뷰 조회 - 삭제되지 않은 리뷰만 생성시간 내림차순")
    void findTop5ByDeletedAtIsNullOrderByCreatedAtDesc_test() {
        //given
        for (int i = 1; i <= 7; i++) {
            Reservation reservation = Reservation.builder()
                    .guest(guest)
                    .host(host)
                    .space(space)
                    .status(ReservationStatus.ACCEPTED)
                    .usagePurpose(UsagePurpose.BARISTA_TRAINING)
                    .requestMessage("테스트")
                    .date(LocalDate.now().minusDays(1))
                    .startTime(LocalTime.of(10, 0, 0))
                    .endTime(LocalTime.of(12, 0, 0))
                    .price(30000)
                    .guestCount(2)
                    .build();
            reservationRepository.save(reservation);

            Review review = Review.builder()
                    .guest(guest)
                    .space(space)
                    .reservation(reservation)
                    .rating(5)
                    .content("리뷰 " + i)
                    .reservedDate(LocalDate.now().minusDays(i))
                    .build();
            reviewRepository.save(review);
        }

        // 하나는 삭제처리
        List<Review> allReviews = reviewRepository.findAll();
        allReviews.get(0).softDelete();

        //when
        List<Review> result = reviewRepository.findTop5ByDeletedAtIsNullOrderByCreatedAtDesc();

        //then
        assertEquals(5, result.size());
        // 삭제되지 않은 리뷰만 조회되었는지 확인
        result.forEach(review -> assertNull(review.getDeletedAt()));
    }

    @Test
    @DisplayName("조건에 맞는 리뷰가 없을 때 빈 결과 반환")
    void findByGuestIdAndSpaceIdAndReservedDateAndDeletedAtIsNull_notFound() {
        //when
        Optional<Review> result = reviewRepository.findByGuestIdAndSpaceIdAndReservedDateAndDeletedAtIsNull(
                999L, 999L, LocalDate.now());

        //then
        assertFalse(result.isPresent());
    }
}
