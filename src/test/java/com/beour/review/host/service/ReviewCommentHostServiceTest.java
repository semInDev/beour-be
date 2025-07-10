package com.beour.review.host.service;

import static org.junit.jupiter.api.Assertions.*;

import com.beour.global.exception.exceptionType.DuplicateException;
import com.beour.global.exception.exceptionType.ReviewCommentNotFoundException;
import com.beour.global.exception.exceptionType.ReviewNotFoundException;
import com.beour.global.exception.exceptionType.UnauthorityException;
import com.beour.global.exception.exceptionType.UserNotFoundException;
import com.beour.reservation.commons.entity.Reservation;
import com.beour.reservation.commons.enums.ReservationStatus;
import com.beour.reservation.commons.enums.UsagePurpose;
import com.beour.reservation.commons.repository.ReservationRepository;
import com.beour.review.domain.entity.Review;
import com.beour.review.domain.entity.ReviewComment;
import com.beour.review.domain.repository.ReviewCommentRepository;
import com.beour.review.domain.repository.ReviewRepository;
import com.beour.review.host.dto.ReviewCommentCreateRequestDto;
import com.beour.review.host.dto.ReviewCommentResponseDto;
import com.beour.review.host.dto.ReviewCommentUpdateRequestDto;
import com.beour.review.host.dto.ReviewCommentableResponseDto;
import com.beour.space.domain.entity.Space;
import com.beour.space.domain.enums.SpaceCategory;
import com.beour.space.domain.enums.UseCategory;
import com.beour.space.domain.repository.SpaceRepository;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
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
class ReviewCommentHostServiceTest {

    @Autowired
    private ReviewCommentHostService reviewCommentHostService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private SpaceRepository spaceRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private ReviewCommentRepository reviewCommentRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    private User guest;
    private User host;
    private User anotherHost;
    private Space space;
    private Space anotherSpace;
    private Review review;
    private Review reviewWithComment;
    private ReviewComment reviewComment;
    private Reservation reservation;
    private Reservation reservationWithComment;

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
                .maxCapacity(3)
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

        reservation = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space)
                .status(ReservationStatus.ACCEPTED)
                .usagePurpose(UsagePurpose.BARISTA_TRAINING)
                .requestMessage("테스트")
                .date(LocalDate.now().minusDays(3))
                .startTime(LocalTime.of(12, 0, 0))
                .endTime(LocalTime.of(16, 0, 0))
                .price(60000)
                .guestCount(2)
                .build();
        reservationRepository.save(reservation);

        reservationWithComment = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space)
                .status(ReservationStatus.ACCEPTED)
                .usagePurpose(UsagePurpose.BARISTA_TRAINING)
                .requestMessage("테스트")
                .date(LocalDate.now().minusDays(3))
                .startTime(LocalTime.of(12, 0, 0))
                .endTime(LocalTime.of(16, 0, 0))
                .price(60000)
                .guestCount(2)
                .build();
        reservationRepository.save(reservationWithComment);

        review = Review.builder()
                .guest(guest)
                .space(space)
                .reservation(reservation)
                .rating(5)
                .content("좋은 공간이었습니다.")
                .reservedDate(LocalDate.now().minusDays(2))
                .build();
        reviewRepository.save(review);

        reviewWithComment = Review.builder()
                .guest(guest)
                .space(space)
                .reservation(reservationWithComment)
                .rating(4)
                .content("괜찮은 공간이었습니다.")
                .reservedDate(LocalDate.now().minusDays(1))
                .build();
        reviewRepository.save(reviewWithComment);

        reviewComment = ReviewComment.builder()
                .user(host)
                .review(reviewWithComment)
                .content("감사합니다!")
                .build();
        reviewCommentRepository.save(reviewComment);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        reviewCommentRepository.deleteAll();
        reviewRepository.deleteAll();
        reservationRepository.deleteAll();
        spaceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("댓글 작성 가능한 리뷰 조회 - 성공")
    void getCommentableReviews_success() {
        // given
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                host.getLoginId(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // when
        List<ReviewCommentableResponseDto> result = reviewCommentHostService.getCommentableReviews();

        // then
        assertEquals(1, result.size());
        assertEquals(review.getId(), result.get(0).getReviewId());
        assertEquals(guest.getNickname(), result.get(0).getGuestNickname());
        assertEquals(review.getRating(), result.get(0).getReviewRating());
        assertEquals(space.getName(), result.get(0).getSpaceName());
    }

    @Test
    @DisplayName("댓글 작성 가능한 리뷰 조회 - 다른 호스트의 공간 제외")
    void getCommentableReviews_excludeOtherHostSpace() {
        // given
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                anotherHost.getLoginId(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // when
        List<ReviewCommentableResponseDto> result = reviewCommentHostService.getCommentableReviews();

        // then
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("작성된 댓글 조회 - 성공")
    void getWrittenReviewComments_success() {
        // given
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                host.getLoginId(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // when
        List<ReviewCommentResponseDto> result = reviewCommentHostService.getWrittenReviewComments();

        // then
        assertEquals(1, result.size());
        assertEquals(guest.getNickname(), result.get(0).getGuestNickname());
        assertEquals(reviewWithComment.getRating(), result.get(0).getReviewRating());
        assertEquals(space.getName(), result.get(0).getSpaceName());
        assertEquals(host.getNickname(), result.get(0).getHostNickname());
        assertEquals(reviewComment.getContent(), result.get(0).getReviewCommentContent());
    }

    @Test
    @DisplayName("작성된 댓글 조회 - 다른 호스트의 댓글 제외")
    void getWrittenReviewComments_excludeOtherHostComments() {
        // given
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                anotherHost.getLoginId(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // when
        List<ReviewCommentResponseDto> result = reviewCommentHostService.getWrittenReviewComments();

        // then
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("리뷰 댓글 작성 - 성공")
    void createReviewComment_success() {
        // given
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                host.getLoginId(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        ReviewCommentCreateRequestDto requestDto = new ReviewCommentCreateRequestDto(
                review.getId(), "감사합니다!");

        // when
        assertDoesNotThrow(() -> reviewCommentHostService.createReviewComment(requestDto));

        // then
        List<ReviewComment> comments = reviewCommentRepository.findAll();
        assertEquals(2, comments.size()); // 기존 1개 + 새로 생성된 1개

        ReviewComment newComment = comments.stream()
                .filter(comment -> comment.getReview().getId().equals(review.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals(host.getId(), newComment.getUser().getId());
        assertEquals(review.getId(), newComment.getReview().getId());
        assertEquals("감사합니다!", newComment.getContent());
    }

    @Test
    @DisplayName("리뷰 댓글 작성 - 존재하지 않는 리뷰")
    void createReviewComment_reviewNotFound() {
        // given
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                host.getLoginId(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        ReviewCommentCreateRequestDto requestDto = new ReviewCommentCreateRequestDto(
                999L, "감사합니다!");

        // when & then
        assertThrows(ReviewNotFoundException.class,
                () -> reviewCommentHostService.createReviewComment(requestDto));
    }

    @Test
    @DisplayName("리뷰 댓글 작성 - 다른 호스트의 공간")
    void createReviewComment_notOwner() {
        // given
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                anotherHost.getLoginId(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        ReviewCommentCreateRequestDto requestDto = new ReviewCommentCreateRequestDto(
                review.getId(), "감사합니다!");

        // when & then
        assertThrows(UnauthorityException.class,
                () -> reviewCommentHostService.createReviewComment(requestDto));
    }

    @Test
    @DisplayName("리뷰 댓글 작성 - 이미 댓글이 존재하는 리뷰")
    void createReviewComment_alreadyExists() {
        // given
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                host.getLoginId(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        ReviewCommentCreateRequestDto requestDto = new ReviewCommentCreateRequestDto(
                reviewWithComment.getId(), "감사합니다!");

        // when & then
        assertThrows(DuplicateException.class,
                () -> reviewCommentHostService.createReviewComment(requestDto));
    }

    @Test
    @DisplayName("리뷰 댓글 수정 - 성공")
    void updateReviewComment_success() {
        // given
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                host.getLoginId(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        ReviewCommentUpdateRequestDto requestDto = new ReviewCommentUpdateRequestDto(
                "수정된 답글입니다!");

        // when
        assertDoesNotThrow(() -> reviewCommentHostService.updateReviewComment(
                reviewComment.getId(), requestDto));

        // then
        ReviewComment updatedComment = reviewCommentRepository.findById(reviewComment.getId())
                .orElseThrow();
        assertEquals("수정된 답글입니다!", updatedComment.getContent());
    }

    @Test
    @DisplayName("리뷰 댓글 수정 - 존재하지 않는 댓글")
    void updateReviewComment_commentNotFound() {
        // given
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                host.getLoginId(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        ReviewCommentUpdateRequestDto requestDto = new ReviewCommentUpdateRequestDto(
                "수정된 답글입니다!");

        // when & then
        assertThrows(ReviewCommentNotFoundException.class,
                () -> reviewCommentHostService.updateReviewComment(999L, requestDto));
    }

    @Test
    @DisplayName("리뷰 댓글 수정 - 다른 호스트의 댓글")
    void updateReviewComment_notOwner() {
        // given
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                anotherHost.getLoginId(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        ReviewCommentUpdateRequestDto requestDto = new ReviewCommentUpdateRequestDto(
                "수정된 답글입니다!");

        // when & then
        assertThrows(UnauthorityException.class,
                () -> reviewCommentHostService.updateReviewComment(reviewComment.getId(), requestDto));
    }

    @Test
    @DisplayName("리뷰 댓글 삭제 - 성공")
    void deleteReviewComment_success() {
        // given
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                host.getLoginId(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // when
        assertDoesNotThrow(() -> reviewCommentHostService.deleteReviewComment(reviewComment.getId()));

        // then
        ReviewComment deletedComment = reviewCommentRepository.findById(reviewComment.getId())
                .orElseThrow();
        assertTrue(deletedComment.isDeleted());
        assertNotNull(deletedComment.getDeletedAt());
    }

    @Test
    @DisplayName("리뷰 댓글 삭제 - 존재하지 않는 댓글")
    void deleteReviewComment_commentNotFound() {
        // given
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                host.getLoginId(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // when & then
        assertThrows(ReviewCommentNotFoundException.class,
                () -> reviewCommentHostService.deleteReviewComment(999L));
    }

    @Test
    @DisplayName("리뷰 댓글 삭제 - 다른 호스트의 댓글")
    void deleteReviewComment_notOwner() {
        // given
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                anotherHost.getLoginId(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // when & then
        assertThrows(UnauthorityException.class,
                () -> reviewCommentHostService.deleteReviewComment(reviewComment.getId()));
    }

    @Test
    @DisplayName("토큰에서 유저 찾기 - 존재하지 않는 유저")
    void findUserFromToken_userNotFound() {
        // given
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "nonexistent", null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // when & then
        assertThrows(UserNotFoundException.class,
                () -> reviewCommentHostService.getCommentableReviews());
    }
}
