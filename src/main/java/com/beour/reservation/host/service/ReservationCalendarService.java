package com.beour.reservation.host.service;

import com.beour.global.exception.exceptionType.UserNotFoundException;
import com.beour.reservation.host.dto.CalendarReservationResponseDto;
import com.beour.reservation.commons.entity.Reservation;
import com.beour.reservation.commons.enums.ReservationStatus;
import com.beour.reservation.commons.repository.ReservationRepository;
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

    public List<CalendarReservationResponseDto> getHostCalendarReservations(LocalDate date) {
        User host = findUserFromToken();

        List<Reservation> reservationList = reservationRepository.findByHostIdAndDateAndDeletedAtIsNull(
                host.getId(), date);

        return convertToCalendarResponseDto(reservationList);
    }

    public List<CalendarReservationResponseDto> getHostPendingReservations(LocalDate date) {
        User host = findUserFromToken();

        List<Reservation> reservationList = reservationRepository.findByHostIdAndDateAndStatusAndDeletedAtIsNull(
                host.getId(), date, ReservationStatus.PENDING);

        return convertToCalendarResponseDto(reservationList);
    }

    public List<CalendarReservationResponseDto> getHostAcceptedReservations(LocalDate date) {
        User host = findUserFromToken();

        List<Reservation> reservationList = reservationRepository.findByHostIdAndDateAndStatusAndDeletedAtIsNull(
                host.getId(), date, ReservationStatus.ACCEPTED);

        return convertToCalendarResponseDto(reservationList);
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
