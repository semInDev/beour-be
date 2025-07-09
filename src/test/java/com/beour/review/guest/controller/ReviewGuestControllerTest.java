package com.beour.review.guest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.beour.global.jwt.JWTUtil;
import com.beour.reservation.commons.entity.Reservation;
import com.beour.reservation.commons.enums.ReservationStatus;
import com.beour.reservation.commons.enums.UsagePurpose;
import com.beour.reservation.commons.repository.ReservationRepository;
import com.beour.review.domain.entity.Review;
import com.beour.review.domain.entity.ReviewComment;
import com.beour.review.domain.entity.ReviewImage;
import com.beour.review.domain.repository.ReviewCommentRepository;
import com.beour.review.domain.repository.ReviewImageRepository;
import com.beour.review.domain.repository.ReviewRepository;
import com.beour.space.domain.entity.Space;
import com.beour.space.domain.enums.SpaceCategory;
import com.beour.space.domain.enums.UseCategory;
import com.beour.space.domain.repository.SpaceRepository;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ReviewGuestControllerTest {

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
    private ReservationRepository reservationRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private ReviewImageRepository reviewImageRepository;
    @Autowired
    private ReviewCommentRepository reviewCommentRepository;

    private User guest;
    private User host;
    private Space space;
    private Reservation completedReservation;
    private Reservation pendingReservation;
    private Review review;
    private String accessToken;

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

        completedReservation = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space)
                .status(ReservationStatus.COMPLETED)
                .usagePurpose(UsagePurpose.BARISTA_TRAINING)
                .requestMessage("테스트 요청")
                .date(LocalDate.now().minusDays(1))
                .startTime(LocalTime.of(13, 0, 0))
                .endTime(LocalTime.of(14, 0, 0))
                .price(15000)
                .guestCount(2)
                .build();
        reservationRepository.save(completedReservation);

        pendingReservation = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space)
                .status(ReservationStatus.PENDING)
                .usagePurpose(UsagePurpose.BARISTA_TRAINING)
                .requestMessage("테스트 요청")
                .date(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(13, 0, 0))
                .endTime(LocalTime.of(14, 0, 0))
                .price(15000)
                .guestCount(2)
                .build();
        reservationRepository.save(pendingReservation);

        review = Review.builder()
                .guest(guest)
                .space(space)
                .reservation(completedReservation)
                .rating(5)
                .content("정말 좋은 공간이었습니다!")
                .reservedDate(completedReservation.getDate())
                .build();
        reviewRepository.save(review);

        accessToken = jwtUtil.createJwt(
                "access",
                guest.getLoginId(),
                "ROLE_" + guest.getRole(),
                1000L * 60 * 30 // 30분
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        reviewImageRepository.deleteAll();
        reviewCommentRepository.deleteAll();
        reviewRepository.deleteAll();
        reservationRepository.deleteAll();
        spaceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("리뷰 가능한 예약 조회 - 성공")
    void getReviewableReservations_success() throws Exception {
        mockMvc.perform(get("/api/guest/reviews/reviewable")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].reservationId").value(completedReservation.getId()))
                .andExpect(jsonPath("$.data[0].spaceName").value(space.getName()))
                .andExpect(jsonPath("$.data[0].date").value(completedReservation.getDate().toString()))
                .andExpect(jsonPath("$.data[0].guestCount").value(2))
                .andExpect(jsonPath("$.data[0].usagePurpose").value("BARISTA_TRAINING"));
    }

    @Test
    @DisplayName("작성한 리뷰 조회 - 성공")
    void getWrittenReviews_success() throws Exception {
        mockMvc.perform(get("/api/guest/reviews/written")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].reviewId").value(review.getId()))
                .andExpect(jsonPath("$.data[0].guestNickname").value(guest.getNickname()))
                .andExpect(jsonPath("$.data[0].reviewRating").value(5))
                .andExpect(jsonPath("$.data[0].spaceName").value(space.getName()))
                .andExpect(jsonPath("$.data[0].reviewContent").value("정말 좋은 공간이었습니다!"));
    }

    @Test
    @DisplayName("리뷰 작성용 예약 정보 조회 - 성공")
    void getReservationForReview_success() throws Exception {
        mockMvc.perform(get("/api/guest/reviews/reservation/" + completedReservation.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reservationId").value(completedReservation.getId()))
                .andExpect(jsonPath("$.data.spaceName").value(space.getName()))
                .andExpect(jsonPath("$.data.date").value(completedReservation.getDate().toString()))
                .andExpect(jsonPath("$.data.guestCount").value(2));
    }

    @Test
    @DisplayName("리뷰 작성용 예약 정보 조회 - 예약 없음")
    void getReservationForReview_not_found() throws Exception {
        mockMvc.perform(get("/api/guest/reviews/reservation/999")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("해당 예약을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("리뷰 생성 - 성공")
    void createReview_success() throws Exception {
        // 새로운 완료된 예약 생성
        Reservation newReservation = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space)
                .status(ReservationStatus.COMPLETED)
                .usagePurpose(UsagePurpose.BARISTA_TRAINING)
                .requestMessage("테스트 요청")
                .date(LocalDate.now().minusDays(2))
                .startTime(LocalTime.of(15, 0, 0))
                .endTime(LocalTime.of(16, 0, 0))
                .price(15000)
                .guestCount(2)
                .build();
        reservationRepository.save(newReservation);

        String requestJson = String.format("""
            {
                "reservationId": %d,
                "rating": 4,
                "content": "괜찮은 공간입니다.",
                "imageUrls": ["https://example.com/image1.jpg", "https://example.com/image2.jpg"]
            }
            """, newReservation.getId());

        mockMvc.perform(post("/api/guest/reviews")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Review가 저장되었습니다."));
    }

    @Test
    @DisplayName("리뷰 생성 - 완료되지 않은 예약")
    void createReview_not_completed_reservation() throws Exception {
        String requestJson = String.format("""
            {
                "reservationId": %d,
                "rating": 4,
                "content": "괜찮은 공간입니다.",
                "imageUrls": []
            }
            """, pendingReservation.getId());

        mockMvc.perform(post("/api/guest/reviews")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("완료된 예약에 대해서만 리뷰를 작성할 수 있습니다."));
    }

    @Test
    @DisplayName("리뷰 생성 - 중복 리뷰")
    void createReview_duplicate_review() throws Exception {
        String requestJson = String.format("""
            {
                "reservationId": %d,
                "rating": 4,
                "content": "괜찮은 공간입니다.",
                "imageUrls": []
            }
            """, completedReservation.getId());

        mockMvc.perform(post("/api/guest/reviews")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 해당 예약에 대한 리뷰가 작성되었습니다."));
    }

    @Test
    @DisplayName("리뷰 생성 - 잘못된 별점")
    void createReview_invalid_rating() throws Exception {
        String requestJson = String.format("""
            {
                "reservationId": %d,
                "rating": 6,
                "content": "괜찮은 공간입니다.",
                "imageUrls": []
            }
            """, completedReservation.getId());

        mockMvc.perform(post("/api/guest/reviews")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("리뷰 생성 - 빈 내용")
    void createReview_empty_content() throws Exception {
        String requestJson = String.format("""
            {
                "reservationId": %d,
                "rating": 4,
                "content": null,
                "imageUrls": []
            }
            """, completedReservation.getId());

        mockMvc.perform(post("/api/guest/reviews")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("리뷰 상세 조회 - 성공")
    void getReviewDetail_success() throws Exception {
        mockMvc.perform(get("/api/guest/reviews/" + review.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reviewId").value(review.getId()))
                .andExpect(jsonPath("$.data.rating").value(5))
                .andExpect(jsonPath("$.data.content").value("정말 좋은 공간이었습니다!"))
                .andExpect(jsonPath("$.data.reservedDate").value(completedReservation.getDate().toString()));
    }

    @Test
    @DisplayName("리뷰 상세 조회 - 존재하지 않는 리뷰")
    void getReviewDetail_not_found() throws Exception {
        mockMvc.perform(get("/api/guest/reviews/999")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("해당 리뷰를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("리뷰 수정 - 성공")
    void updateReview_success() throws Exception {
        String requestJson = """
            {
                "rating": 4,
                "content": "수정된 리뷰 내용입니다.",
                "imageUrls": ["https://example.com/updated.jpg"]
            }
            """;

        mockMvc.perform(patch("/api/guest/reviews/" + review.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Review가 수정되었습니다."));
    }

    @Test
    @DisplayName("리뷰 수정 - 존재하지 않는 리뷰")
    void updateReview_not_found() throws Exception {
        String requestJson = """
            {
                "rating": 4,
                "content": "수정된 리뷰 내용입니다.",
                "imageUrls": []
            }
            """;

        mockMvc.perform(patch("/api/guest/reviews/999")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("해당 리뷰를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("리뷰 수정 - 잘못된 별점")
    void updateReview_invalid_rating() throws Exception {
        String requestJson = """
            {
                "rating": 0,
                "content": "수정된 리뷰 내용입니다.",
                "imageUrls": []
            }
            """;

        mockMvc.perform(patch("/api/guest/reviews/" + review.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("리뷰 수정 - 빈 내용")
    void updateReview_blank_content() throws Exception {
        String requestJson = """
            {
                "rating": 4,
                "content": "",
                "imageUrls": []
            }
            """;

        mockMvc.perform(patch("/api/guest/reviews/" + review.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    @DisplayName("리뷰 삭제 - 성공")
    void deleteReview_success() throws Exception {
        mockMvc.perform(delete("/api/guest/reviews/" + review.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Review가 삭제되었습니다."));
    }

    @Test
    @DisplayName("리뷰 삭제 - 존재하지 않는 리뷰")
    void deleteReview_not_found() throws Exception {
        mockMvc.perform(delete("/api/guest/reviews/999")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("해당 리뷰를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("최신 리뷰 조회 - 성공")
    void getNewReviews_success() throws Exception {
        mockMvc.perform(get("/api/reviews/new")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].spaceName").value(space.getName()))
                .andExpect(jsonPath("$.data[0].reviewerNickName").value(guest.getNickname()))
                .andExpect(jsonPath("$.data[0].rating").value(5))
                .andExpect(jsonPath("$.data[0].reviewContent").value("정말 좋은 공간이었습니다!"));
    }

    @Test
    @DisplayName("최신 리뷰 조회 - 리뷰 없음")
    void getNewReviews_empty() throws Exception {
        // 기존 리뷰 삭제
        reviewRepository.deleteAll();

        mockMvc.perform(get("/api/reviews/new")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("최근 등록된 리뷰가 없습니다."));
    }

    @Test
    @DisplayName("다른 사용자의 리뷰 접근 시도 - 권한 없음")
    void accessOtherUserReview_unauthorized() throws Exception {
        // 다른 사용자 생성
        User otherUser = User.builder()
                .loginId("other")
                .password(passwordEncoder.encode("password!"))
                .name("다른사용자")
                .nickname("other")
                .email("other@gmail.com")
                .phone("01098765432")
                .role("GUEST")
                .build();
        userRepository.save(otherUser);

        String otherAccessToken = jwtUtil.createJwt(
                "access",
                otherUser.getLoginId(),
                "ROLE_" + otherUser.getRole(),
                1000L * 60 * 30
        );

        mockMvc.perform(get("/api/guest/reviews/" + review.getId())
                        .header("Authorization", "Bearer " + otherAccessToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("해당 리뷰에 대한 권한이 없습니다."));
    }

    @Test
    @DisplayName("다른 사용자의 예약으로 리뷰 작성 시도 - 권한 없음")
    void createReviewWithOtherUserReservation_unauthorized() throws Exception {
        // 다른 사용자 생성
        User otherUser = User.builder()
                .loginId("other")
                .password(passwordEncoder.encode("password!"))
                .name("다른사용자")
                .nickname("other")
                .email("other@gmail.com")
                .phone("01098765432")
                .role("GUEST")
                .build();
        userRepository.save(otherUser);

        // 다른 사용자의 예약 생성
        Reservation otherReservation = Reservation.builder()
                .guest(otherUser)
                .host(host)
                .space(space)
                .status(ReservationStatus.COMPLETED)
                .usagePurpose(UsagePurpose.BARISTA_TRAINING)
                .requestMessage("테스트 요청")
                .date(LocalDate.now().minusDays(3))
                .startTime(LocalTime.of(15, 0, 0))
                .endTime(LocalTime.of(16, 0, 0))
                .price(15000)
                .guestCount(2)
                .build();
        reservationRepository.save(otherReservation);

        String requestJson = String.format("""
            {
                "reservationId": %d,
                "rating": 4,
                "content": "괜찮은 공간입니다.",
                "imageUrls": []
            }
            """, otherReservation.getId());

        mockMvc.perform(post("/api/guest/reviews")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("해당 예약에 대한 권한이 없습니다."));
    }
}
