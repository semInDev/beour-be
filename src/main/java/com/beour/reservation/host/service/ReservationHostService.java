package com.beour.reservation.host.service;

import com.beour.global.exception.error.errorcode.ReservationErrorCode;
import com.beour.global.exception.error.errorcode.SpaceErrorCode;
import com.beour.global.exception.error.errorcode.UserErrorCode;
import com.beour.global.exception.exceptionType.SpaceNotFoundException;
import com.beour.global.exception.exceptionType.UnauthorityException;
import com.beour.global.exception.exceptionType.UserNotFoundException;
import com.beour.reservation.commons.entity.Reservation;
import com.beour.reservation.commons.enums.ReservationStatus;
import com.beour.reservation.commons.exceptionType.ReservationNotFound;
import com.beour.reservation.commons.repository.ReservationRepository;
import com.beour.reservation.host.dto.HostReservationListResponseDto;
import com.beour.reservation.host.dto.HostSpaceListResponseDto;
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
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ReservationHostService {

    private final SpaceRepository spaceRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;

    public List<HostSpaceListResponseDto> getHostSpaces() {
        User host = findUserFromToken();

        List<Space> spaceList = spaceRepository.findByHostAndDeletedAtIsNull(host);

        if (spaceList.isEmpty()) {
            throw new SpaceNotFoundException(SpaceErrorCode.NO_HOST_SPACE);
        }

        List<HostSpaceListResponseDto> responseDtoList = new ArrayList<>();
        for (Space space : spaceList) {
            responseDtoList.add(HostSpaceListResponseDto.of(space));
        }

        return responseDtoList;
    }

    @Transactional(readOnly = true)
    public List<HostReservationListResponseDto> getHostReservationsByDate(LocalDate date) {
        User host = findUserFromToken();

        List<Reservation> reservationList = reservationRepository.findByHostIdAndDateAndDeletedAtIsNull(
                host.getId(), date);

        return filterAcceptedReservationsAndConvert(reservationList);
    }

    public List<HostReservationListResponseDto> getHostReservationsByDateAndSpace(LocalDate date, Long spaceId) {
        User host = findUserFromToken();

        Space space = spaceRepository.findById(spaceId).orElseThrow(
                () -> new SpaceNotFoundException(SpaceErrorCode.SPACE_NOT_FOUND)
        );

        // 호스트가 해당 공간의 소유자인지 확인
        if (!space.getHost().getId().equals(host.getId())) {
            throw new UnauthorityException(SpaceErrorCode.NO_PERMISSION);
        }

        List<Reservation> reservationList = reservationRepository.findByHostIdAndDateAndSpaceIdAndDeletedAtIsNull(
                host.getId(), date, spaceId);

        return filterAcceptedReservationsAndConvert(reservationList);
    }

    private List<HostReservationListResponseDto> filterAcceptedReservationsAndConvert(List<Reservation> reservationList) {
        List<Reservation> acceptedReservations = reservationList.stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.ACCEPTED)
                .collect(Collectors.toList());

        if (acceptedReservations.isEmpty()) {
            throw new ReservationNotFound(ReservationErrorCode.RESERVATION_NOT_FOUND);
        }

        return acceptedReservations.stream()
                .map(HostReservationListResponseDto::of)
                .collect(Collectors.toList());
    }

    private User findUserFromToken() {
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByLoginIdAndDeletedAtIsNull(loginId).orElseThrow(
                () -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND)
        );
    }
}
