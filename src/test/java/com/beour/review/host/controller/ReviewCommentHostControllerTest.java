package com.beour.review.host.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import com.beour.review.domain.repository.ReviewCommentRepository;
import com.beour.review.domain.repository.ReviewRepository;
import com.beour.space.domain.entity.Space;
import com.beour.space.domain.enums.SpaceCategory;
import com.beour.space.domain.enums.UseCategory;
import com.beour.space.domain.repository.SpaceRepository;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
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
class ReviewCommentHostControllerTest {

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
    private ReviewCommentRepository reviewCommentRepository;

    private User guest;
    private User host;
    private User otherHost;
    private Space space;
    private Space otherSpace;
    private Reservation reservation;
    private Review review;
    private Review reviewWithComment;
    private ReviewComment reviewComment;
    private String hostAccessToken;
    private String otherHostAccessToken;

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
                .phone("01087654321")
                .role("HOST")
                .build();
        userRepository.save(otherHost);

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
        spaceRepository.save(otherSpace);

        reservation = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space)
                .status(ReservationStatus.COMPLETED)
                .usagePurpose(UsagePurpose.BARISTA_TRAINING)
                .requestMessage("테스트 예약")
                .date(LocalDate.now().minusDays(1))
                .startTime(LocalTime.of(13, 0, 0))
                .endTime(LocalTime.of(14, 0, 0))
                .price(15000)
                .guestCount(2)
                .build();
        reservationRepository.save(reservation);

        review = Review.builder()
                .guest(guest)
                .space(space)
                .reservation(reservation)
                .rating(5)
                .content("정말 좋은 공간이었습니다!")
                .reservedDate(LocalDate.now().minusDays(1))
                .images(new ArrayList<>())
                .build();
        reviewRepository.save(review);

        // 이미 댓글이 있는 리뷰 생성
        Reservation reservation2 = Reservation.builder()
                .guest(guest)
                .host(host)
                .space(space)
                .status(ReservationStatus.COMPLETED)
                .usagePurpose(UsagePurpose.BARISTA_TRAINING)
                .requestMessage("테스트 예약2")
                .date(LocalDate.now().minusDays(2))
                .startTime(LocalTime.of(15, 0, 0))
                .endTime(LocalTime.of(16, 0, 0))
                .price(15000)
                .guestCount(2)
                .build();
        reservationRepository.save(reservation2);

        reviewWithComment = Review.builder()
                .guest(guest)
                .space(space)
                .reservation(reservation2)
                .rating(4)
                .content("좋은 공간입니다.")
                .reservedDate(LocalDate.now().minusDays(2))
                .images(new ArrayList<>())
                .build();
        reviewRepository.save(reviewWithComment);

        reviewComment = ReviewComment.builder()
                .user(host)
                .review(reviewWithComment)
                .content("감사합니다!")
                .build();
        reviewCommentRepository.save(reviewComment);

        hostAccessToken = jwtUtil.createJwt(
                "access",
                host.getLoginId(),
                "ROLE_" + host.getRole(),
                1000L * 60 * 30    // 30분
        );

        otherHostAccessToken = jwtUtil.createJwt(
                "access",
                otherHost.getLoginId(),
                "ROLE_" + otherHost.getRole(),
                1000L * 60 * 30    // 30분
        );
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
    void get_commentable_reviews_success() throws Exception {
        mockMvc.perform(get("/api/host/review-comments/commentable")
                        .header("Authorization", "Bearer " + hostAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].reviewId").value(review.getId()))
                .andExpect(jsonPath("$.data[0].guestNickname").value(guest.getNickname()))
                .andExpect(jsonPath("$.data[0].reviewRating").value(review.getRating()))
                .andExpect(jsonPath("$.data[0].spaceName").value(space.getName()))
                .andExpect(jsonPath("$.data[0].reviewContent").value(review.getContent()));
    }

    @Test
    @DisplayName("작성한 댓글 조회 - 성공")
    void get_written_review_comments_success() throws Exception {
        mockMvc.perform(get("/api/host/review-comments/written")
                        .header("Authorization", "Bearer " + hostAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].guestNickname").value(guest.getNickname()))
                .andExpect(jsonPath("$.data[0].reviewRating").value(reviewWithComment.getRating()))
                .andExpect(jsonPath("$.data[0].spaceName").value(space.getName()))
                .andExpect(jsonPath("$.data[0].reviewContent").value(reviewWithComment.getContent()))
                .andExpect(jsonPath("$.data[0].hostNickname").value(host.getNickname()))
                .andExpect(jsonPath("$.data[0].reviewCommentContent").value(reviewComment.getContent()));
    }

    @Test
    @DisplayName("리뷰 댓글 작성 - 성공")
    void create_review_comment_success() throws Exception {
        String requestJson = String.format("""
            {
                "reviewId": %d,
                "content": "좋은 후기 감사합니다!"
            }
            """, review.getId());

        mockMvc.perform(post("/api/host/review-comments")
                        .header("Authorization", "Bearer " + hostAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("답글이 저장되었습니다."));
    }

    @Test
    @DisplayName("리뷰 댓글 작성 - 존재하지 않는 리뷰")
    void create_review_comment_review_not_found() throws Exception {
        String requestJson = """
            {
                "reviewId": 999,
                "content": "좋은 후기 감사합니다!"
            }
            """;

        mockMvc.perform(post("/api/host/review-comments")
                        .header("Authorization", "Bearer " + hostAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 리뷰입니다."));
    }

    @Test
    @DisplayName("리뷰 댓글 작성 - 다른 호스트의 공간 리뷰")
    void create_review_comment_not_space_owner() throws Exception {
        String requestJson = String.format("""
            {
                "reviewId": %d,
                "content": "좋은 후기 감사합니다!"
            }
            """, review.getId());

        mockMvc.perform(post("/api/host/review-comments")
                        .header("Authorization", "Bearer " + otherHostAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("해당 리뷰의 공간 소유자가 아닙니다."));
    }

    @Test
    @DisplayName("리뷰 댓글 작성 - 이미 댓글이 존재하는 리뷰")
    void create_review_comment_already_exists() throws Exception {
        String requestJson = String.format("""
            {
                "reviewId": %d,
                "content": "좋은 후기 감사합니다!"
            }
            """, reviewWithComment.getId());

        mockMvc.perform(post("/api/host/review-comments")
                        .header("Authorization", "Bearer " + hostAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 댓글이 작성된 리뷰입니다."));
    }

    @Test
    @DisplayName("리뷰 댓글 작성 - 내용이 비어있음")
    void create_review_comment_empty_content() throws Exception {
        String requestJson = String.format("""
            {
                "reviewId": %d,
                "content": ""
            }
            """, review.getId());

        mockMvc.perform(post("/api/host/review-comments")
                        .header("Authorization", "Bearer " + hostAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("답글 내용은 비어 있을 수 없습니다."));
    }

    @Test
    @DisplayName("리뷰 댓글 작성 - reviewId가 null")
    void create_review_comment_null_review_id() throws Exception {
        String requestJson = """
            {
                "reviewId": null,
                "content": "좋은 후기 감사합니다!"
            }
            """;

        mockMvc.perform(post("/api/host/review-comments")
                        .header("Authorization", "Bearer " + hostAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("reviewId는 필수입니다."));
    }

    @Test
    @Transactional
    @DisplayName("리뷰 댓글 수정 - 성공")
    void update_review_comment_success() throws Exception {
        String requestJson = """
            {
                "content": "수정된 댓글 내용입니다."
            }
            """;

        mockMvc.perform(patch("/api/host/review-comments/" + reviewComment.getId())
                        .header("Authorization", "Bearer " + hostAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("답글이 수정되었습니다."));

        ReviewComment updatedComment = reviewCommentRepository.findById(reviewComment.getId()).get();
        assertEquals("수정된 댓글 내용입니다.", updatedComment.getContent());
    }

    @Test
    @DisplayName("리뷰 댓글 수정 - 존재하지 않는 댓글")
    void update_review_comment_not_found() throws Exception {
        String requestJson = """
            {
                "content": "수정된 댓글 내용입니다."
            }
            """;

        mockMvc.perform(patch("/api/host/review-comments/999")
                        .header("Authorization", "Bearer " + hostAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 댓글입니다."));
    }

    @Test
    @DisplayName("리뷰 댓글 수정 - 다른 호스트의 댓글")
    void update_review_comment_not_owner() throws Exception {
        String requestJson = """
            {
                "content": "수정된 댓글 내용입니다."
            }
            """;

        mockMvc.perform(patch("/api/host/review-comments/" + reviewComment.getId())
                        .header("Authorization", "Bearer " + otherHostAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("댓글 수정 권한이 없습니다."));
    }

    @Test
    @DisplayName("리뷰 댓글 수정 - 내용이 비어있음")
    void update_review_comment_empty_content() throws Exception {
        String requestJson = """
            {
                "content": ""
            }
            """;

        mockMvc.perform(patch("/api/host/review-comments/" + reviewComment.getId())
                        .header("Authorization", "Bearer " + hostAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("답글 내용은 비어 있을 수 없습니다."));
    }

    @Test
    @Transactional
    @DisplayName("리뷰 댓글 삭제 - 성공")
    void delete_review_comment_success() throws Exception {
        mockMvc.perform(delete("/api/host/review-comments/" + reviewComment.getId())
                        .header("Authorization", "Bearer " + hostAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("답글이 삭제되었습니다."));

        ReviewComment deletedComment = reviewCommentRepository.findById(reviewComment.getId()).get();
        assertNotNull(deletedComment.getDeletedAt());
    }

    @Test
    @DisplayName("리뷰 댓글 삭제 - 존재하지 않는 댓글")
    void delete_review_comment_not_found() throws Exception {
        mockMvc.perform(delete("/api/host/review-comments/999")
                        .header("Authorization", "Bearer " + hostAccessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 댓글입니다."));
    }

    @Test
    @DisplayName("리뷰 댓글 삭제 - 다른 호스트의 댓글")
    void delete_review_comment_not_owner() throws Exception {
        mockMvc.perform(delete("/api/host/review-comments/" + reviewComment.getId())
                        .header("Authorization", "Bearer " + otherHostAccessToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("댓글 삭제 권한이 없습니다."));
    }
}
