package com.beour.reservation.guest.service;

import com.beour.global.exception.exceptionType.SpaceNotFoundException;
import com.beour.global.exception.exceptionType.UserNotFoundException;
import com.beour.reservation.commons.entity.Reservation;
import com.beour.reservation.commons.enums.ReservationStatus;
import com.beour.reservation.commons.repository.ReservationRepository;
import com.beour.reservation.guest.dto.ReservationCreateRequest;
import com.beour.space.host.entity.Space;
import com.beour.space.host.repository.SpaceRepository;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ReservationGuestService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final SpaceRepository spaceRepository;

    public void createReservation(ReservationCreateRequest requestDto){
        User guest = userRepository.findById(requestDto.getGuestId()).orElseThrow(
            () -> new UserNotFoundException("존재하지 않는 유저입니다.")
        );
        User host = userRepository.findById(requestDto.getHostId()).orElseThrow(
            () -> new UserNotFoundException("존재하지 않는 유저입니다.")
        );
        Space space = spaceRepository.findById(requestDto.getSpaceId()).orElseThrow(
            () -> new SpaceNotFoundException("존재하지 않는 공간입니다.")
        );

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
            .build();

        reservationRepository.save(reservation);
    }

}
