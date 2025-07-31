package com.beour.reservation.host.service;

import com.beour.global.exception.error.errorcode.ReservationErrorCode;
import com.beour.global.exception.error.errorcode.SpaceErrorCode;
import com.beour.global.exception.error.errorcode.UserErrorCode;
import com.beour.global.exception.exceptionType.SpaceNotFoundException;
import com.beour.global.exception.exceptionType.UnauthorityException;
import com.beour.global.exception.exceptionType.UserNotFoundException;
import com.beour.global.exception.exceptionType.MissMatch;
import com.beour.reservation.host.dto.CalendarReservationPageResponseDto;
import com.beour.reservation.host.dto.CalendarReservationResponseDto;
import com.beour.reservation.commons.entity.Reservation;
import com.beour.reservation.commons.enums.ReservationStatus;
import com.beour.global.exception.exceptionType.ReservationNotFound;
import com.beour.reservation.commons.repository.ReservationRepository;
import com.beour.space.domain.entity.Space;
import com.beour.space.domain.repository.SpaceRepository;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ReservationCalendarService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final SpaceRepository spaceRepository;

    @Transactional(readOnly = true)
    public CalendarReservationPageResponseDto getHostCalendarReservations(LocalDate date, Long spaceId, Pageable pageable) {
        User host = findUserFromToken();
        Page<Reservation> reservationPage = getReservationPageDependingOnSpaceId(host, date, spaceId, null, pageable);
        return convertToCalendarPageResponseDto(reservationPage);
    }

    @Transactional(readOnly = true)
    public CalendarReservationPageResponseDto getHostPendingReservations(LocalDate date, Long spaceId, Pageable pageable) {
        User host = findUserFromToken();
        Page<Reservation> reservationPage = getReservationPageDependingOnSpaceId(host, date, spaceId, ReservationStatus.PENDING, pageable);
        return convertToCalendarPageResponseDto(reservationPage);
    }

    @Transactional(readOnly = true)
    public CalendarReservationPageResponseDto getHostAcceptedReservations(LocalDate date, Long spaceId, Pageable pageable) {
        User host = findUserFromToken();
        Page<Reservation> reservationPage = getReservationPageDependingOnSpaceId(host, date, spaceId, ReservationStatus.ACCEPTED, pageable);
        return convertToCalendarPageResponseDto(reservationPage);
    }

    @Transactional
    public void acceptReservation(Long reservationId, Long spaceId) {
        User host = findUserFromToken();
        Reservation reservation = validateReservationAndSpaceOwnership(reservationId, spaceId, host);
        reservation.updateStatus(ReservationStatus.ACCEPTED);
    }

    @Transactional
    public void rejectReservation(Long reservationId, Long spaceId) {
        User host = findUserFromToken();
        Reservation reservation = validateReservationAndSpaceOwnership(reservationId, spaceId, host);
        reservation.updateStatus(ReservationStatus.REJECTED);
    }

    private Reservation validateReservationAndSpaceOwnership(Long reservationId, Long spaceId, User host) {
        // 예약 존재 확인
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFound(ReservationErrorCode.RESERVATION_NOT_FOUND));

        // 공간 존재 확인
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new SpaceNotFoundException(SpaceErrorCode.SPACE_NOT_FOUND));

        // 공간 소유자 확인
        validateSpaceOwnership(host, spaceId);

        // 예약의 공간과 입력받은 공간이 일치하는지 확인
        if (!reservation.getSpace().getId().equals(spaceId)) {
            throw new MissMatch(ReservationErrorCode.SPACE_MISMATCH);
        }

        // 예약의 호스트와 현재 사용자가 일치하는지 확인
        if (!reservation.getHost().getId().equals(host.getId())) {
            throw new UnauthorityException(ReservationErrorCode.NO_PERMISSION);
        }

        return reservation;
    }

    private Page<Reservation> getReservationPageDependingOnSpaceId(User host, LocalDate date, Long spaceId, ReservationStatus status, Pageable pageable) {
        if (spaceId != null) {
            validateSpaceOwnership(host, spaceId);
            if (status != null) {
                return reservationRepository.findByHostIdAndDateAndSpaceIdAndStatusAndDeletedAtIsNullOrderByStartTime(
                        host.getId(), date, spaceId, status, pageable
                );
            } else {
                return reservationRepository.findByHostIdAndDateAndSpaceIdAndDeletedAtIsNullOrderByStartTime(
                        host.getId(), date, spaceId, pageable
                );
            }
        } else {
            if (status != null) {
                return reservationRepository.findByHostIdAndDateAndStatusAndDeletedAtIsNullOrderByStartTime(
                        host.getId(), date, status, pageable
                );
            } else {
                return reservationRepository.findByHostIdAndDateAndDeletedAtIsNullOrderByStartTime(
                        host.getId(), date, pageable
                );
            }
        }
    }

    private void validateSpaceOwnership(User host, Long spaceId) {
        Space space = spaceRepository.findById(spaceId).orElseThrow(
                () -> new SpaceNotFoundException(SpaceErrorCode.SPACE_NOT_FOUND)
        );

        if (!space.getHost().getId().equals(host.getId())) {
            throw new UnauthorityException(SpaceErrorCode.NO_PERMISSION);
        }
    }

    private CalendarReservationPageResponseDto convertToCalendarPageResponseDto(Page<Reservation> reservationPage) {
        List<CalendarReservationResponseDto> responseDtoList = new ArrayList<>();
        for (Reservation reservation : reservationPage.getContent()) {
            responseDtoList.add(CalendarReservationResponseDto.of(reservation));
        }

        return new CalendarReservationPageResponseDto(
                responseDtoList,
                reservationPage.isLast(),
                reservationPage.getTotalPages()
        );
    }

    private User findUserFromToken() {
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByLoginIdAndDeletedAtIsNull(loginId).orElseThrow(
                () -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND)
        );
    }
}
