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
import com.beour.reservation.host.dto.HostReservationListPageResponseDto;
import com.beour.reservation.host.dto.HostReservationListResponseDto;
import com.beour.reservation.host.dto.HostSpaceListResponseDto;
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
    public HostReservationListPageResponseDto getHostReservationsByDate(LocalDate date, Pageable pageable) {
        User host = findUserFromToken();

        Page<Reservation> reservationPage = reservationRepository.findByHostIdAndDateAndDeletedAtIsNullOrderByStartTime(
                host.getId(), date, pageable);

        return filterAcceptedReservationsAndConvert(reservationPage);
    }

    public HostReservationListPageResponseDto getHostReservationsByDateAndSpace(LocalDate date, Long spaceId, Pageable pageable) {
        User host = findUserFromToken();

        Space space = spaceRepository.findById(spaceId).orElseThrow(
                () -> new SpaceNotFoundException(SpaceErrorCode.SPACE_NOT_FOUND)
        );

        // 호스트가 해당 공간의 소유자인지 확인
        if (!space.getHost().getId().equals(host.getId())) {
            throw new UnauthorityException(SpaceErrorCode.NO_PERMISSION);
        }

        Page<Reservation> reservationPage = reservationRepository.findByHostIdAndDateAndSpaceIdAndDeletedAtIsNullOrderByStartTime(
                host.getId(), date, spaceId, pageable);

        return filterAcceptedReservationsAndConvert(reservationPage);
    }

    private HostReservationListPageResponseDto filterAcceptedReservationsAndConvert(Page<Reservation> reservationPage) {
        List<Reservation> acceptedReservations = reservationPage.getContent().stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.ACCEPTED)
                .collect(Collectors.toList());

        if (acceptedReservations.isEmpty()) {
            throw new ReservationNotFound(ReservationErrorCode.RESERVATION_NOT_FOUND);
        }

        List<HostReservationListResponseDto> responseDtoList = acceptedReservations.stream()
                .map(HostReservationListResponseDto::of)
                .collect(Collectors.toList());

        return new HostReservationListPageResponseDto(
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
