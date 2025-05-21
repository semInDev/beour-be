package com.beour.reservation.guest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReservationCreateRequest {

    @NotBlank(message = "예약자의 id 값 필수")
    private Long guestId;

    @NotBlank(message = "호스트의 id 값 필수")
    private Long hostId;

    @NotBlank(message = "예약할 공간 id 값 필수")
    private Long spaceId;

    @NotBlank(message = "예약일자 필수")
    private LocalDate date;

    @NotBlank(message = "이용 시작 시간 필수")
    private LocalTime startTime;

    @NotBlank(message = "이용 마감 시간 필수")
    private LocalTime endTime;

    @Min(0)
    @NotBlank(message = "총 가격 필수")
    private int price;

    @Min(1)
    @NotBlank(message = "사용 인원 필수")
    private int guestCount;

}
