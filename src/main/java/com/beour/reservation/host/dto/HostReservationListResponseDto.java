package com.beour.reservation.host.dto;

import com.beour.reservation.commons.entity.Reservation;
import com.beour.reservation.commons.enums.ReservationStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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
    private boolean isCurrentlyInUse;

    @Builder
    private HostReservationListResponseDto(Long reservationId, String guestName, ReservationStatus status,
                                           String spaceName, LocalTime startTime, LocalTime endTime,
                                           int guestCount, boolean isCurrentlyInUse) {
        this.reservationId = reservationId;
        this.guestName = guestName;
        this.status = status;
        this.spaceName = spaceName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.guestCount = guestCount;
        this.isCurrentlyInUse = isCurrentlyInUse;
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
                .isCurrentlyInUse(calculateCurrentlyInUse(reservation))
                .build();
    }

    private static boolean calculateCurrentlyInUse(Reservation reservation) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // 서비스에서 이미 ACCEPTED 상태만 필터링되어 전달되므로,
        // 예약 날짜가 오늘이며, 현재 시간이 시작시간과 종료시간 사이에 있는 경우만 확인
        return reservation.getDate().equals(today) &&
                !now.isBefore(reservation.getStartTime()) &&
                now.isBefore(reservation.getEndTime());
    }
}
