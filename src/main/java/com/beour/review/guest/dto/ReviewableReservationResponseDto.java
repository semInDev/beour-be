package com.beour.review.guest.dto;

import com.beour.reservation.commons.entity.Reservation;
import com.beour.reservation.commons.enums.UsagePurpose;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewableReservationResponseDto {

    private Long reservationId;
    private String spaceName;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private int guestCount;
    private UsagePurpose usagePurpose;

    public static ReviewableReservationResponseDto of(Reservation reservation) {
        return ReviewableReservationResponseDto.builder()
                .reservationId(reservation.getId())
                .spaceName(reservation.getSpace().getName())
                .date(reservation.getDate())
                .startTime(reservation.getStartTime())
                .endTime(reservation.getEndTime())
                .guestCount(reservation.getGuestCount())
                .usagePurpose(reservation.getUsagePurpose())
                .build();
    }
}
