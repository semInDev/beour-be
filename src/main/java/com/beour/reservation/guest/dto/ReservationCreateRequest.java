package com.beour.reservation.guest.dto;

import com.beour.reservation.commons.enums.UsagePurpose;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReservationCreateRequest {

    @NotNull(message = "호스트의 id 값 필수")
    private Long hostId;

    @NotNull(message = "예약할 공간 id 값 필수")
    private Long spaceId;

    @NotNull(message = "예약일자 필수")
    private LocalDate date;

    @NotNull(message = "이용 시작 시간 필수")
    private LocalTime startTime;

    @NotNull(message = "이용 마감 시간 필수")
    private LocalTime endTime;

    @Min(0)
    private int price;

    @Min(1)
    private int guestCount;

    @NotNull(message = "이용 목적 필수")
    private UsagePurpose usagePurpose;

    @Size(max = 200, message = "요청 사항은 200자 이내로 입력해주세요.")
    private String requestMessage;
}
