package com.beour.review.guest.service;

import com.beour.global.exception.exceptionType.UserNotFoundException;
import com.beour.reservation.commons.entity.Reservation;
import com.beour.reservation.commons.enums.ReservationStatus;
import com.beour.reservation.commons.repository.ReservationRepository;
import com.beour.review.domain.entity.Review;
import com.beour.review.domain.entity.ReviewComment;
import com.beour.review.domain.repository.ReviewRepository;
import com.beour.review.guest.dto.ReviewableReservationResponseDto;
import com.beour.review.guest.dto.WrittenReviewResponseDto;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ReviewGuestService {

    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;
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

    private User findUserFromToken() {
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByLoginIdAndDeletedAtIsNull(loginId).orElseThrow(
                () -> new UserNotFoundException("해당 유저를 찾을 수 없습니다.")
        );
    }
}
