package com.beour.reservation.guest.service;

import com.beour.global.exception.error.errorcode.AvailableTimeErrorCode;
import com.beour.global.exception.error.errorcode.ReservationErrorCode;
import com.beour.global.exception.error.errorcode.SpaceErrorCode;
import com.beour.global.exception.error.errorcode.UserErrorCode;
import com.beour.global.exception.exceptionType.SpaceNotFoundException;
import com.beour.global.exception.exceptionType.UserNotFoundException;
import com.beour.reservation.commons.entity.Reservation;
import com.beour.reservation.commons.enums.ReservationStatus;
import com.beour.reservation.commons.exceptionType.AvailableTimeNotFound;
import com.beour.reservation.commons.exceptionType.MissMatch;
import com.beour.reservation.commons.exceptionType.ReservationNotFound;
import com.beour.reservation.commons.repository.ReservationRepository;
import com.beour.reservation.guest.dto.CheckAvailableTimesRequestDto;
import com.beour.reservation.guest.dto.ReservationCreateRequest;
import com.beour.reservation.guest.dto.ReservationListResponseDto;
import com.beour.reservation.guest.dto.ReservationResponseDto;
import com.beour.review.domain.entity.Review;
import com.beour.review.domain.repository.ReviewRepository;
import com.beour.space.domain.entity.AvailableTime;
import com.beour.space.domain.entity.Space;
import com.beour.space.domain.repository.SpaceRepository;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ReservationGuestService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final SpaceRepository spaceRepository;
    private final CheckAvailableTimeService checkAvailableTimeService;
    private final ReviewRepository reviewRepository;

    public ReservationResponseDto createReservation(ReservationCreateRequest requestDto) {
        User guest = findUserFromToken();
        User host = userRepository.findById(requestDto.getHostId()).orElseThrow(
            () -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND)
        );
        Space space = spaceRepository.findById(requestDto.getSpaceId()).orElseThrow(
            () -> new SpaceNotFoundException(SpaceErrorCode.SPACE_NOT_FOUND)
        );

        checkReservationAvailable(requestDto, space);

        Reservation reservation = Reservation.builder()
            .guest(guest)
            .host(host)
            .space(space)
            .status(ReservationStatus.PENDING)
            .date(requestDto.getDate())
            .startTime(requestDto.getStartTime())
            .endTime(requestDto.getEndTime())
            .price(requestDto.getPrice())
            .guestCount(requestDto.getGuestCount())
            .usagePurpose(requestDto.getUsagePurpose())
            .requestMessage(requestDto.getRequestMessage())
            .build();

        return ReservationResponseDto.of(reservationRepository.save(reservation));
    }

    private void checkReservationAvailable(ReservationCreateRequest requestDto, Space space) {
        checkPriceCorrect(requestDto, space);
        checkValidCapacity(requestDto, space);
        checkReservationAvailableDate(requestDto);
        checkReservationAvailableTime(requestDto);
    }

    private static void checkValidCapacity(ReservationCreateRequest requestDto, Space space) {
        if (requestDto.getGuestCount() > space.getMaxCapacity()) {
            throw new MissMatch(ReservationErrorCode.INVALID_CAPACITY);
        }
    }

    private static void checkPriceCorrect(ReservationCreateRequest requestDto, Space space) {
        int hour = requestDto.getEndTime().getHour() - requestDto.getStartTime().getHour();
        if (requestDto.getPrice() != space.getPricePerHour() * hour) {
            throw new MissMatch(ReservationErrorCode.INVALID_PRICE);
        }
    }

    private void checkReservationAvailableDate(ReservationCreateRequest requestDto) {
        AvailableTime availableTime = checkAvailableTimeService.checkReservationAvailableDateAndGetAvailableTime(
            new CheckAvailableTimesRequestDto(
                requestDto.getSpaceId(), requestDto.getDate()));

        if (requestDto.getDate().equals(LocalDate.now()) && requestDto.getStartTime()
            .isBefore(LocalTime.now())) {
            throw new AvailableTimeNotFound(AvailableTimeErrorCode.AVAILABLE_TIME_NOT_FOUND);
        }

        if (availableTime.getStartTime().isAfter(requestDto.getStartTime())
            || availableTime.getEndTime().isBefore(
            requestDto.getEndTime())) {
            throw new MissMatch(AvailableTimeErrorCode.TIME_UNAVAILABLE);
        }
    }

    private void checkReservationAvailableTime(ReservationCreateRequest requestDto) {
        List<Reservation> reservationList = reservationRepository.findBySpaceIdAndDateAndDeletedAtIsNull(
            requestDto.getSpaceId(), requestDto.getDate());

        LocalTime startTime = requestDto.getStartTime();
        while (startTime.isBefore(requestDto.getEndTime())) {
            LocalTime currentTime = requestDto.getStartTime();

            boolean isReserved = reservationList.stream().anyMatch(reservation ->
                reservation.getStartTime().isBefore(currentTime.plusHours(1)) &&
                    reservation.getEndTime().isAfter(currentTime)
            );

            if (isReserved) {
                throw new MissMatch(AvailableTimeErrorCode.TIME_UNAVAILABLE);
            }

            startTime = startTime.plusHours(1);
        }
    }

    public List<ReservationListResponseDto> findReservationList() {
        User guest = findUserFromToken();
        List<Reservation> reservationList = reservationRepository.findUpcomingReservationsByGuest(
            guest.getId(), LocalDate.now(), LocalTime.now());

        checkEmptyReservation(reservationList);

        List<ReservationListResponseDto> responseDtoList = new ArrayList<>();
        for (Reservation reservation : reservationList) {
            responseDtoList.add(ReservationListResponseDto.of(reservation));
        }

        return responseDtoList;
    }

    @Transactional
    public List<ReservationListResponseDto> findPastReservationList() {
        User user = findUserFromToken();
        List<Reservation> reservationList = reservationRepository.findPastReservationsByGuest(
            user.getId(), LocalDate.now(), LocalTime.now());

        checkEmptyReservation(reservationList);

        List<ReservationListResponseDto> responseDtoList = new ArrayList<>();
        for (Reservation reservation : reservationList) {
            if (reservation.getStatus() == ReservationStatus.ACCEPTED) {
                reservation.updateStatus(ReservationStatus.COMPLETED);
            }
            Review review = reviewRepository.findByGuestIdAndSpaceIdAndReservedDateAndDeletedAtIsNull(
                user.getId(), reservation.getSpace().getId(), reservation.getDate()).orElse(null);
            Long reviewId = 0L;
            if (review != null) {
                reviewId = review.getId();
            }
            responseDtoList.add(ReservationListResponseDto.of(reservation, reviewId));
        }

        return responseDtoList;
    }

    private static void checkEmptyReservation(List<Reservation> reservationList) {
        if (reservationList.isEmpty()) {
            throw new ReservationNotFound(ReservationErrorCode.RESERVATION_NOT_FOUND);
        }
    }

    @Transactional
    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(
            () -> new ReservationNotFound(ReservationErrorCode.RESERVATION_NOT_FOUND)
        );

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new MissMatch(ReservationErrorCode.CANNOT_CANCEL_RESERVATION);
        }

        reservation.cancel();
    }

    private User findUserFromToken() {
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByLoginIdAndDeletedAtIsNull(loginId).orElseThrow(
            () -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND)
        );
    }
}
