package com.beour.review.guest.service;

import com.beour.global.exception.error.errorcode.ReservationErrorCode;
import com.beour.global.exception.error.errorcode.ReviewErrorCode;
import com.beour.global.exception.error.errorcode.UserErrorCode;
import com.beour.global.exception.exceptionType.DuplicateException;
import com.beour.global.exception.exceptionType.ReviewNotFoundException;
import com.beour.global.exception.exceptionType.UnauthorityException;
import com.beour.global.exception.exceptionType.UserNotFoundException;
import com.beour.global.file.ImageUploader;
import com.beour.reservation.commons.entity.Reservation;
import com.beour.reservation.commons.enums.ReservationStatus;
import com.beour.reservation.commons.exceptionType.MissMatch;
import com.beour.reservation.commons.exceptionType.ReservationNotFound;
import com.beour.reservation.commons.repository.ReservationRepository;
import com.beour.review.domain.entity.Review;
import com.beour.review.domain.entity.ReviewComment;
import com.beour.review.domain.entity.ReviewImage;
import com.beour.review.domain.repository.ReviewRepository;
import com.beour.review.guest.dto.RecentWrittenReviewResponseDto;
import com.beour.review.guest.dto.ReviewDetailResponseDto;
import com.beour.review.guest.dto.ReviewForReservationResponseDto;
import com.beour.review.guest.dto.ReviewRequestDto;
import com.beour.review.guest.dto.ReviewUpdateRequestDto;
import com.beour.review.guest.dto.ReviewableReservationPageResponseDto;
import com.beour.review.guest.dto.ReviewableReservationResponseDto;
import com.beour.review.guest.dto.WrittenReviewPageResponseDto;
import com.beour.review.guest.dto.WrittenReviewResponseDto;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ReviewGuestService {

    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ImageUploader imageUploader;

    public ReviewableReservationPageResponseDto getReviewableReservations(Pageable pageable) {
        User guest = findUserFromToken();

        Page<Reservation> completedReservations = reservationRepository
                .findCompletedReservationsByGuestId(guest.getId(), pageable);

        checkEmptyReservation(completedReservations);

        List<ReviewableReservationResponseDto> reservations = completedReservations.getContent().stream()
                .map(ReviewableReservationResponseDto::of)
                .toList();

        return new ReviewableReservationPageResponseDto(
                reservations,
                completedReservations.isLast(),
                completedReservations.getTotalPages()
        );
    }

    public WrittenReviewPageResponseDto getWrittenReviews(Pageable pageable) {
        User guest = findUserFromToken();

        Page<Review> writtenReviews = reviewRepository.findAllWithCommentAndImagesByGuestIdPaged(guest.getId(), pageable);

        checkEmptyReviews(writtenReviews);

        List<WrittenReviewResponseDto> reviews = writtenReviews.getContent().stream()
                .map(review -> {
                    ReviewComment comment = review.getComment();
                    return WrittenReviewResponseDto.of(review, comment);
                })
                .toList();

        return new WrittenReviewPageResponseDto(
                reviews,
                writtenReviews.isLast(),
                writtenReviews.getTotalPages()
        );
    }

    // 예약 정보 조회 (리뷰 작성을 위한)
    public ReviewForReservationResponseDto getReservationForReview(Long reservationId) {
        User guest = findUserFromToken();
        Reservation reservation = findReservationById(reservationId);

        validateReservationOwner(reservation, guest);

        return ReviewForReservationResponseDto.of(reservation);
    }

    @Transactional
    public void createReview(ReviewRequestDto requestDto, List<MultipartFile> images) throws IOException {
        User guest = findUserFromToken();
        Reservation reservation = findReservationById(requestDto.getReservationId());

        validateReservationOwner(reservation, guest);
        validateReservationStatus(reservation);
        checkDuplicateReview(guest.getId(), reservation.getSpace().getId(), reservation.getDate());

        Review review = buildReview(guest, reservation, requestDto.getRating(), requestDto.getContent());
        Review savedReview = reviewRepository.save(review);

        saveReviewImages(savedReview, images);
    }

    public ReviewDetailResponseDto getReviewDetail(Long reviewId) {
        User guest = findUserFromToken();
        Review review = findReviewById(reviewId);

        validateReviewOwner(review, guest);

        return ReviewDetailResponseDto.of(review);
    }

    @Transactional
    public void updateReview(Long reviewId, ReviewUpdateRequestDto requestDto, List<MultipartFile> images) throws IOException {
        User guest = findUserFromToken();
        Review review = findReviewById(reviewId);

        validateReviewOwner(review, guest);

        review.updateRating(requestDto.getRating());
        review.updateContent(requestDto.getContent());

        updateReviewImages(review, images);
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        User guest = findUserFromToken();
        Review review = findReviewById(reviewId);

        validateReviewOwner(review, guest);

        review.softDelete();
    }

    public List<RecentWrittenReviewResponseDto> getRecentWrittenReviews(){
        List<Review> reviews = reviewRepository.findTop5ByDeletedAtIsNullOrderByCreatedAtDesc();

        if(reviews.isEmpty()){
            throw new ReviewNotFoundException(ReviewErrorCode.NO_RECENT_REVIEW);
        }

        return reviews.stream()
                .map(review -> {
                    List<String> imageUrls = Optional.ofNullable(review.getImages())
                            .orElse(Collections.emptyList())
                            .stream()
                            .map(ReviewImage::getImageUrl)
                            .toList();

                    return RecentWrittenReviewResponseDto.builder()
                            .spaceName(review.getSpace().getName())
                            .reviewerNickName(review.getGuest().getNickname())
                            .reviewCreatedAt(review.getCreatedAt())
                            .rating(review.getRating())
                            .images(imageUrls)
                            .reviewContent(review.getContent())
                            .build();
                }).collect(Collectors.toList());
    }

    private User findUserFromToken() {
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByLoginIdAndDeletedAtIsNull(loginId).orElseThrow(
                () -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND)
        );
    }

    private Reservation findReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId).orElseThrow(
                () -> new ReservationNotFound(ReservationErrorCode.RESERVATION_NOT_FOUND)
        );
    }

    private Review findReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId).orElseThrow(
                () -> new ReviewNotFoundException(ReviewErrorCode.REVIEW_NOT_FOUND)
        );
    }

    private void validateReservationOwner(Reservation reservation, User guest) {
        if (!reservation.getGuest().getId().equals(guest.getId())) {
            throw new UnauthorityException(ReservationErrorCode.NO_PERMISSION);
        }
    }

    private void validateReservationStatus(Reservation reservation) {
        if (reservation.getStatus() != ReservationStatus.COMPLETED) {
            throw new MissMatch(ReviewErrorCode.ONLY_COMPLETED_CAN_REVIEW);
        }
    }

    private void checkDuplicateReview(Long guestId, Long spaceId, java.time.LocalDate reservedDate) {
        if (reviewRepository.findByGuestIdAndSpaceIdAndReservedDateAndDeletedAtIsNull(
                guestId, spaceId, reservedDate).isPresent()) {
            throw new DuplicateException(ReviewErrorCode.REVIEW_ALREADY_EXISTS);
        }
    }

    private void validateReviewOwner(Review review, User guest) {
        if (!review.getGuest().getId().equals(guest.getId())) {
            throw new UnauthorityException(ReviewErrorCode.NO_PERMISSION);
        }
    }

    private Review buildReview(User guest, Reservation reservation, int rating, String content) {
        return Review.builder()
                .guest(guest)
                .space(reservation.getSpace())
                .reservation(reservation)
                .rating(rating)
                .content(content)
                .reservedDate(reservation.getDate())
                .build();
    }

    private void saveReviewImages(Review review, List<MultipartFile> images) throws IOException {
        if (images != null && !images.isEmpty()) {
            List<ReviewImage> reviewImages = new ArrayList<>();

            for (MultipartFile image : images) {
                String imageUrl = imageUploader.upload(image);
                ReviewImage reviewImage = ReviewImage.builder()
                        .imageUrl(imageUrl)
                        .build();
                reviewImages.add(reviewImage);
            }

            for(ReviewImage image : reviewImages) {
                review.addImage(image);
            }
            reviewRepository.save(review);
        }
    }

    private void updateReviewImages(Review review, List<MultipartFile> images) throws IOException {
        // 기존 이미지 삭제
        deleteExistingImages(review);

        // 새로운 이미지 저장
        saveReviewImages(review, images);
    }

    private void deleteExistingImages(Review review) {
        List<ReviewImage> existingImages = review.getImages();
        if (existingImages != null) {
            review.getImages().clear();
        }
    }

    private void checkEmptyReservation(Page<Reservation> reservations) {
        if (reservations.isEmpty()) {
            throw new ReservationNotFound(ReservationErrorCode.RESERVATION_NOT_FOUND);
        }
    }

    private void checkEmptyReviews(Page<Review> reviews) {
        if (reviews.isEmpty()) {
            throw new ReviewNotFoundException(ReviewErrorCode.REVIEW_NOT_FOUND);
        }
    }
}
