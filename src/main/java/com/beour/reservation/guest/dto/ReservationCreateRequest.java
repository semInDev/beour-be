package com.beour.reservation.guest.dto;

import com.beour.reservation.commons.enums.UsagePurpose;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservationCreateRequest {

    @NotNull(message = "예약일자 필수")
    private LocalDate date;

    @NotNull(message = "이용 시작 시간 필수")
    private LocalTime startTime;

    @NotNull(message = "이용 마감 시간 필수")
    private LocalTime endTime;

    @Min(value = 0, message = "가격은 0원부터 입력가능합니다.")
    private int price;

    @Min(value = 1, message = "최소 인원은 1명입니다.")
    private int guestCount;

    @NotNull(message = "이용 목적 필수")
    private UsagePurpose usagePurpose;

    @Size(max = 200, message = "요청 사항은 200자 이내로 입력해주세요.")
    private String requestMessage;
}
