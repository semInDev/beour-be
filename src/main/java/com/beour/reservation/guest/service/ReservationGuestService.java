package com.beour.reservation.guest.service;

import com.beour.global.exception.exceptionType.SpaceNotFoundException;
import com.beour.global.exception.exceptionType.UserNotFoundException;
import com.beour.reservation.commons.entity.Reservation;
import com.beour.reservation.commons.enums.ReservationStatus;
import com.beour.reservation.commons.exceptionType.AvailableTimeNotFound;
import com.beour.reservation.commons.exceptionType.ReservationNotFound;
import com.beour.reservation.commons.repository.ReservationRepository;
import com.beour.reservation.guest.dto.CheckAvailableTimesRequestDto;
import com.beour.reservation.guest.dto.ReservationCreateRequest;
import com.beour.reservation.guest.dto.ReservationListResponseDto;
import com.beour.reservation.guest.dto.ReservationResponseDto;
import com.beour.space.domain.entity.Space;
import com.beour.space.domain.repository.AvailableTimeRepository;
import com.beour.space.domain.repository.SpaceRepository;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ReservationGuestService {

    private final ReservationRepository reservationRepository;
    private final AvailableTimeRepository availableTimeRepository;
    private final UserRepository userRepository;
    private final SpaceRepository spaceRepository;
    private final CheckAvailableTimeService checkAvailableTimeService;

    public ReservationResponseDto createReservation(ReservationCreateRequest requestDto){
        User guest = getUser(requestDto.getGuestId());
        User host = getUser(requestDto.getHostId());
        Space space = spaceRepository.findById(requestDto.getSpaceId()).orElseThrow(
            () -> new SpaceNotFoundException("존재하지 않는 공간입니다.")
        );

//        if(!availableTimeRepository.existsBySpaceIdAndDateAndDeletedAtIsNull(requestDto.getSpaceId(), requestDto.getDate())){
//            throw new AvailableTimeNotFound("해당 일은 예약이 불가합니다.");
//        }

        checkAvailableTimeService.findAvailableTime(new CheckAvailableTimesRequestDto(requestDto.getSpaceId(), requestDto.getDate()));

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

        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationResponseDto.of(savedReservation);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(
            () -> new UserNotFoundException("존재하지 않는 유저입니다.")
        );
    }

    public List<ReservationListResponseDto> findReservationList(Long guestId){
        List<Reservation> reservationList = reservationRepository.findUpcomingReservationsByGuest(guestId, LocalDate.now(), LocalTime.now());

        checkEmptyReservation(reservationList);

        List<ReservationListResponseDto> responseDtoList = new ArrayList<>();
        for (Reservation reservation : reservationList) {
            responseDtoList.add(ReservationListResponseDto.of(reservation));
        }


        return responseDtoList;
    }

    public List<ReservationListResponseDto> findPastReservationList(Long guestId){
        List<Reservation> reservationList = reservationRepository.findPastReservationsByGuest(guestId, LocalDate.now(), LocalTime.now());

        checkEmptyReservation(reservationList);

        List<ReservationListResponseDto> responseDtoList = new ArrayList<>();
        for (Reservation reservation : reservationList) {
            responseDtoList.add(ReservationListResponseDto.of(reservation));
        }


        return responseDtoList;
    }

    private static void checkEmptyReservation(List<Reservation> reservationList) {
        if(reservationList.isEmpty()){
            throw new ReservationNotFound("예약이 없습니다.");
        }
    }

}
