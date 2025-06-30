package com.beour.reservation.host.service;

import com.beour.global.exception.exceptionType.SpaceNotFoundException;
import com.beour.global.exception.exceptionType.UserNotFoundException;
import com.beour.reservation.commons.entity.Reservation;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
            throw new RuntimeException("해당 호스트가 등록한 공간이 없습니다.");
        }

        List<HostSpaceListResponseDto> responseDtoList = new ArrayList<>();
        for (Space space : spaceList) {
            responseDtoList.add(HostSpaceListResponseDto.of(space));
        }

        return responseDtoList;
    }

    public List<HostReservationListResponseDto> getHostReservationsByDate(LocalDate date) {
        User host = findUserFromToken();

        List<Reservation> reservationList = reservationRepository.findByHostIdAndDateAndDeletedAtIsNull(
                host.getId(), date);

        if (reservationList.isEmpty()) {
            throw new ReservationNotFound("해당 날짜에 예약이 없습니다.");
        }

        List<HostReservationListResponseDto> responseDtoList = new ArrayList<>();
        for (Reservation reservation : reservationList) {
            responseDtoList.add(HostReservationListResponseDto.of(reservation));
        }

        return responseDtoList;
    }

    public List<HostReservationListResponseDto> getHostReservationsByDateAndSpace(LocalDate date, Long spaceId) {
        User host = findUserFromToken();

        Space space = spaceRepository.findById(spaceId).orElseThrow(
                () -> new SpaceNotFoundException("존재하지 않는 공간입니다.")
        );

        // 호스트가 해당 공간의 소유자인지 확인
        if (!space.getHost().getId().equals(host.getId())) {
            throw new IllegalArgumentException("해당 공간의 소유자가 아닙니다.");
        }

        List<Reservation> reservationList = reservationRepository.findByHostIdAndDateAndSpaceIdAndDeletedAtIsNull(
                host.getId(), date, spaceId);

        if (reservationList.isEmpty()) {
            throw new ReservationNotFound("해당 날짜와 공간에 예약이 없습니다.");
        }

        List<HostReservationListResponseDto> responseDtoList = new ArrayList<>();
        for (Reservation reservation : reservationList) {
            responseDtoList.add(HostReservationListResponseDto.of(reservation));
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
