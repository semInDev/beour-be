package com.beour.review.guest.service;

import com.beour.global.exception.exceptionType.UserNotFoundException;
import com.beour.reservation.commons.entity.Reservation;
import com.beour.reservation.commons.enums.ReservationStatus;
import com.beour.reservation.commons.exceptionType.ReservationNotFound;
import com.beour.reservation.commons.repository.ReservationRepository;
import com.beour.review.domain.entity.Review;
import com.beour.review.domain.entity.ReviewComment;
import com.beour.review.domain.entity.ReviewImage;
import com.beour.review.domain.repository.ReviewImageRepository;
import com.beour.review.domain.repository.ReviewRepository;
import com.beour.review.guest.dto.ReviewDetailResponseDto;
import com.beour.review.guest.dto.ReviewForReservationResponseDto;
import com.beour.review.guest.dto.ReviewRequestDto;
import com.beour.review.guest.dto.ReviewUpdateRequestDto;
import com.beour.review.guest.dto.ReviewableReservationResponseDto;
import com.beour.review.guest.dto.WrittenReviewResponseDto;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ReviewGuestService {

    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final UserRepository userRepository;

    public List<ReviewableReservationResponseDto> getReviewableReservations() {
        User guest = findUserFromToken();

        // COMPLETED 상태의 예약만 조회
        List<Reservation> completedReservations = reservationRepository.findAll()
                .stream()
                .filter(reservation -> reservation.getGuest().getId().equals(guest.getId()))
                .filter(reservation -> reservation.getStatus() == ReservationStatus.COMPLETED)
                .filter(reservation -> reservation.getDeletedAt() == null)
                .collect(Collectors.toList());

        return completedReservations.stream()
                .map(ReviewableReservationResponseDto::of)
                .collect(Collectors.toList());
    }

    public List<WrittenReviewResponseDto> getWrittenReviews() {
        User guest = findUserFromToken();

        // 게스트가 작성한 리뷰 조회
        List<Review> writtenReviews = reviewRepository.findAll()
                .stream()
                .filter(review -> review.getGuest().getId().equals(guest.getId()))
                .filter(review -> review.getDeletedAt() == null)
                .collect(Collectors.toList());

        return writtenReviews.stream()
                .map(review -> {
                    ReviewComment comment = review.getComment();
                    return WrittenReviewResponseDto.of(review, comment);
                })
                .collect(Collectors.toList());
    }

    // 예약 정보 조회 (리뷰 작성을 위한)
    public ReviewForReservationResponseDto getReservationForReview(Long reservationId) {
        User guest = findUserFromToken();
        Reservation reservation = findReservationById(reservationId);

        validateReservationOwner(reservation, guest);

        return ReviewForReservationResponseDto.of(reservation);
    }

    @Transactional
    public void createReview(ReviewRequestDto requestDto) {
        User guest = findUserFromToken();
        Reservation reservation = findReservationById(requestDto.getReservationId());

        validateReservationOwner(reservation, guest);

        // 이미 리뷰가 작성되었는지 확인
        if (reviewRepository.findByGuestIdAndSpaceIdAndReservedDateAndDeletedAtIsNull(
                guest.getId(), reservation.getSpace().getId(), reservation.getDate()).isPresent()) {
            throw new IllegalArgumentException("이미 해당 예약에 대한 리뷰가 작성되었습니다.");
        }

        Review review = Review.builder()
                .guest(guest)
                .space(reservation.getSpace())
                .reservation(reservation)
                .rating(requestDto.getRating())
                .content(requestDto.getContent())
                .reservedDate(reservation.getDate())
                .build();

        Review savedReview = reviewRepository.save(review);

        saveReviewImages(savedReview, requestDto.getImageUrls());
    }

    public ReviewDetailResponseDto getReviewDetail(Long reviewId) {
        User guest = findUserFromToken();
        Review review = findReviewById(reviewId);

        validateReviewOwner(review, guest);

        return ReviewDetailResponseDto.of(review);
    }

    @Transactional
    public void updateReview(Long reviewId, ReviewUpdateRequestDto requestDto) {
        User guest = findUserFromToken();
        Review review = findReviewById(reviewId);

        validateReviewOwner(review, guest);

        // 리뷰 업데이트
        review.updateRating(requestDto.getRating());
        review.updateContent(requestDto.getContent());

        // 기존 이미지 삭제 후 새로운 이미지 저장
        deleteExistingImages(review);
        saveReviewImages(review, requestDto.getImageUrls());
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        User guest = findUserFromToken();
        Review review = findReviewById(reviewId);

        validateReviewOwner(review, guest);

        review.softDelete();
    }

    private User findUserFromToken() {
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByLoginIdAndDeletedAtIsNull(loginId).orElseThrow(
                () -> new UserNotFoundException("해당 유저를 찾을 수 없습니다.")
        );
    }

    private Reservation findReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId).orElseThrow(
                () -> new ReservationNotFound("해당 예약을 찾을 수 없습니다.")
        );
    }

    private Review findReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId).orElseThrow(
                () -> new RuntimeException("해당 리뷰를 찾을 수 없습니다.")
        );
    }

    private void validateReservationOwner(Reservation reservation, User guest) {
        if (!reservation.getGuest().getId().equals(guest.getId())) {
            throw new IllegalArgumentException("해당 예약에 대한 권한이 없습니다.");
        }
    }

    private void validateReviewOwner(Review review, User guest) {
        if (!review.getGuest().getId().equals(guest.getId())) {
            throw new IllegalArgumentException("해당 리뷰에 대한 권한이 없습니다.");
        }
    }

    private void saveReviewImages(Review review, List<String> imageUrls) {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            List<ReviewImage> images = imageUrls.stream()
                    .map(url -> ReviewImage.builder()
                            .review(review)
                            .imageUrl(url)
                            .build())
                    .collect(Collectors.toList());

            reviewImageRepository.saveAll(images);
        }
    }

    private void deleteExistingImages(Review review) {
        List<ReviewImage> existingImages = review.getImages();
        if (!existingImages.isEmpty()) {
            reviewImageRepository.deleteAll(existingImages);
        }
    }
}
