package com.beour.reservation.host.service;

import com.beour.global.exception.exceptionType.SpaceNotFoundException;
import com.beour.global.exception.exceptionType.UserNotFoundException;
import com.beour.reservation.host.dto.CalendarReservationResponseDto;
import com.beour.reservation.commons.entity.Reservation;
import com.beour.reservation.commons.enums.ReservationStatus;
import com.beour.reservation.commons.repository.ReservationRepository;
import com.beour.space.domain.entity.Space;
import com.beour.space.domain.repository.SpaceRepository;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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

        List<Reservation> reservationList;

        if (spaceId != null) {
            validateSpaceOwnership(host, spaceId);
            reservationList = reservationRepository.findByHostIdAndDateAndSpaceIdAndDeletedAtIsNull(
                    host.getId(), date, spaceId);
        } else {
            reservationList = reservationRepository.findByHostIdAndDateAndDeletedAtIsNull(
                    host.getId(), date);
        }

        return convertToCalendarResponseDto(reservationList);
    }

    public List<CalendarReservationResponseDto> getHostPendingReservations(LocalDate date, Long spaceId) {
        User host = findUserFromToken();

        List<Reservation> reservationList;

        if (spaceId != null) {
            validateSpaceOwnership(host, spaceId);
            reservationList = reservationRepository.findByHostIdAndDateAndSpaceIdAndStatusAndDeletedAtIsNull(
                    host.getId(), date, spaceId, ReservationStatus.PENDING);
        } else {
            reservationList = reservationRepository.findByHostIdAndDateAndStatusAndDeletedAtIsNull(
                    host.getId(), date, ReservationStatus.PENDING);
        }

        return convertToCalendarResponseDto(reservationList);
    }

    public List<CalendarReservationResponseDto> getHostAcceptedReservations(LocalDate date, Long spaceId) {
        User host = findUserFromToken();

        List<Reservation> reservationList;

        if (spaceId != null) {
            validateSpaceOwnership(host, spaceId);
            reservationList = reservationRepository.findByHostIdAndDateAndSpaceIdAndStatusAndDeletedAtIsNull(
                    host.getId(), date, spaceId, ReservationStatus.ACCEPTED);
        } else {
            reservationList = reservationRepository.findByHostIdAndDateAndStatusAndDeletedAtIsNull(
                    host.getId(), date, ReservationStatus.ACCEPTED);
        }

        return convertToCalendarResponseDto(reservationList);
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
                () -> new UserNotFoundException("해당 유저를 찾을 수 없습니다.")
        );
    }
}
