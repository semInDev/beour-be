package com.beour.reservation.host.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class HostReservationListPageResponseDto {
    private List<HostReservationListResponseDto> reservations;
    private boolean last;
    private int totalPage;
}
