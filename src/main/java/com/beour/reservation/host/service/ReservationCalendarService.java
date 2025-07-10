package com.beour.reservation.host.service;

import com.beour.global.exception.error.errorcode.UserErrorCode;
import com.beour.global.exception.exceptionType.SpaceNotFoundException;
import com.beour.global.exception.exceptionType.UserNotFoundException;
import com.beour.reservation.host.dto.CalendarReservationResponseDto;
import com.beour.reservation.commons.entity.Reservation;
import com.beour.reservation.commons.enums.ReservationStatus;
import com.beour.reservation.commons.exceptionType.ReservationNotFound;
import com.beour.reservation.commons.repository.ReservationRepository;
import com.beour.space.domain.entity.Space;
import com.beour.space.domain.repository.SpaceRepository;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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

    public List<CalendarReservationResponseDto> getHostCalendarReservations(LocalDate date, Long spaceId) {
        User host = findUserFromToken();
        List<Reservation> reservationList = getReservationListDependingOnSpaceId(host, date, spaceId, null);
        return convertToCalendarResponseDto(reservationList);
    }

    public List<CalendarReservationResponseDto> getHostPendingReservations(LocalDate date, Long spaceId) {
        User host = findUserFromToken();
        List<Reservation> reservationList = getReservationListDependingOnSpaceId(host, date, spaceId, ReservationStatus.PENDING);
        return convertToCalendarResponseDto(reservationList);
    }

    public List<CalendarReservationResponseDto> getHostAcceptedReservations(LocalDate date, Long spaceId) {
        User host = findUserFromToken();
        List<Reservation> reservationList = getReservationListDependingOnSpaceId(host, date, spaceId, ReservationStatus.ACCEPTED);
        return convertToCalendarResponseDto(reservationList);
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
                .orElseThrow(() -> new ReservationNotFound("존재하지 않는 예약입니다."));

        // 공간 존재 확인
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new SpaceNotFoundException("존재하지 않는 공간입니다."));

        // 공간 소유자 확인
        validateSpaceOwnership(host, spaceId);

        // 예약의 공간과 입력받은 공간이 일치하는지 확인
        if (!reservation.getSpace().getId().equals(spaceId)) {
            throw new IllegalArgumentException("예약과 공간 정보가 일치하지 않습니다.");
        }

        // 예약의 호스트와 현재 사용자가 일치하는지 확인
        if (!reservation.getHost().getId().equals(host.getId())) {
            throw new IllegalArgumentException("해당 예약의 호스트가 아닙니다.");
        }

        return reservation;
    }

    private List<Reservation> getReservationListDependingOnSpaceId(User host, LocalDate date, Long spaceId, ReservationStatus status) {
        if (spaceId != null) {
            validateSpaceOwnership(host, spaceId);
            if (status != null) {
                return reservationRepository.findByHostIdAndDateAndSpaceIdAndStatusAndDeletedAtIsNull(
                        host.getId(), date, spaceId, status
                );
            } else {
                return reservationRepository.findByHostIdAndDateAndSpaceIdAndDeletedAtIsNull(
                        host.getId(), date, spaceId
                );
            }
        } else {
            if (status != null) {
                return reservationRepository.findByHostIdAndDateAndStatusAndDeletedAtIsNull(
                        host.getId(), date, status
                );
            } else {
                return reservationRepository.findByHostIdAndDateAndDeletedAtIsNull(
                        host.getId(), date
                );
            }
        }
    }

    private void validateSpaceOwnership(User host, Long spaceId) {
        Space space = spaceRepository.findById(spaceId).orElseThrow(
                () -> new SpaceNotFoundException("존재하지 않는 공간입니다.")
        );

        if (!space.getHost().getId().equals(host.getId())) {
            throw new IllegalArgumentException("해당 공간의 소유자가 아닙니다.");
        }
    }

    private List<CalendarReservationResponseDto> convertToCalendarResponseDto(List<Reservation> reservationList) {
        List<CalendarReservationResponseDto> responseDtoList = new ArrayList<>();
        for (Reservation reservation : reservationList) {
            responseDtoList.add(CalendarReservationResponseDto.of(reservation));
        }
        return responseDtoList;
    }

    private User findUserFromToken() {
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByLoginIdAndDeletedAtIsNull(loginId).orElseThrow(
                () -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND)
        );
    }
}
