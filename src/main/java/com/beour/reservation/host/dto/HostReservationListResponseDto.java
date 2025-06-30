package com.beour.reservation.host.dto;

import com.beour.reservation.commons.entity.Reservation;
import com.beour.reservation.commons.enums.ReservationStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class HostReservationListResponseDto {

    private Long reservationId;
    private String guestName;
    private ReservationStatus status;
    private String spaceName;
    private LocalTime startTime;
    private LocalTime endTime;
    private int guestCount;

    @Builder
    private HostReservationListResponseDto(Long reservationId, String guestName, ReservationStatus status,
                                           String spaceName, LocalTime startTime, LocalTime endTime, int guestCount) {
        this.reservationId = reservationId;
        this.guestName = guestName;
        this.status = status;
        this.spaceName = spaceName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.guestCount = guestCount;
    }

    public static HostReservationListResponseDto of(Reservation reservation) {
        return HostReservationListResponseDto.builder()
                .reservationId(reservation.getId())
                .guestName(reservation.getGuest().getName())
                .status(reservation.getStatus())
                .spaceName(reservation.getSpace().getName())
                .startTime(reservation.getStartTime())
                .endTime(reservation.getEndTime())
                .guestCount(reservation.getGuestCount())
                .build();
    }
}
