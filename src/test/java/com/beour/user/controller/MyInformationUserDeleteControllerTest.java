package com.beour.user.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

import com.beour.global.exception.error.errorcode.ReservationErrorCode;
import com.beour.global.jwt.JWTUtil;
import com.beour.reservation.commons.entity.Reservation;
import com.beour.reservation.commons.enums.ReservationStatus;
import com.beour.reservation.commons.enums.UsagePurpose;
import com.beour.reservation.commons.repository.ReservationRepository;
import com.beour.space.domain.entity.Space;
import com.beour.space.domain.enums.SpaceCategory;
import com.beour.space.domain.enums.UseCategory;
import com.beour.space.domain.repository.SpaceRepository;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class MyInformationUserDeleteControllerTest {


    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SpaceRepository spaceRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private JWTUtil jwtUtil;
    @Autowired
    private MockMvc mockMvc;

    private User guest;
    private String guestToken;
    private User host;
    private String hostToken;
    private Space space;

    @BeforeEach
    void setUp() {
        guest = User.builder()
            .loginId("guest")
            .password("guestpw")
            .name("게스트")
            .nickname("guest")
            .email("guest@gmail.com")
            .phone("01012345678")
            .role("GUEST")
            .build();
        userRepository.save(guest);

        guestToken = jwtUtil.createJwt(
            "access",
            guest.getLoginId(),
            "ROLE_" + guest.getRole(),
            1000L * 60 * 30    // 30분
        );

        host = User.builder()
            .loginId("host")
            .password("hostpw")
            .name("게스트")
            .nickname("host")
            .email("host@gmail.com")
            .phone("01012345678")
            .role("HOST")
            .build();
        userRepository.save(host);

        hostToken = jwtUtil.createJwt(
            "access",
            host.getLoginId(),
            "ROLE_" + host.getRole(),
            1000L * 60 * 30    // 30분
        );

        space = Space.builder()
            .host(host)
            .name("공간1")
            .spaceCategory(SpaceCategory.COOKING)
            .useCategory(UseCategory.COOKING)
            .maxCapacity(3)
            .address("서울시 강남구")
            .detailAddress("투썸건물 2층")
            .pricePerHour(15000)
            .thumbnailUrl("https://example.img")
            .latitude(123.12)
            .longitude(123.12)
            .avgRating(0.0)
            .availableTimes(new ArrayList<>())
            .build();
        spaceRepository.save(space);
    }

    @AfterEach
    void tearDown() {
        reservationRepository.deleteAll();
        spaceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @Transactional
    @DisplayName("회원 탈퇴(게스트) - 성공")
    void success_userWithdraw_guest() throws Exception{
        //when  then
        mockMvc.perform(delete("/api/users")
                .header("Authorization", "Bearer " + guestToken)
            )
            .andExpect(status().isOk());

        assertTrue(guest.isDeleted());
    }

    @Test
    @Transactional
    @DisplayName("회원 탈퇴(게스트) - 실패(예약 남아있음)")
    void userWithdraw_with_reservation_future_guest() throws Exception{
        //given
        Reservation reservation = Reservation.builder()
            .guest(guest)
            .host(host)
            .space(space)
            .status(ReservationStatus.ACCEPTED)
            .usagePurpose(UsagePurpose.BARISTA_TRAINING)
            .requestMessage("테슽뚜")
            .date(LocalDate.now().plusDays(1))
            .startTime(LocalTime.of(12, 0, 0))
            .endTime(LocalTime.of(16, 0, 0))
            .price(60000)
            .guestCount(2)
            .build();
        reservationRepository.save(reservation);

        //when  then
        mockMvc.perform(delete("/api/users")
                .header("Authorization", "Bearer " + guestToken)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(ReservationErrorCode.FUTURE_RESERVATION_REMAIN.getMessage()));

        assertFalse(guest.isDeleted());
    }

    @Test
    @Transactional
    @DisplayName("회원 탈퇴(호스트) - 성공")
    void success_userWithdraw_host() throws Exception{
        //when  then
        mockMvc.perform(delete("/api/users")
                .header("Authorization", "Bearer " + hostToken)
            )
            .andExpect(status().isOk());

        assertTrue(host.isDeleted());
        assertTrue(space.isDeleted());
    }

    @Test
    @Transactional
    @DisplayName("회원 탈퇴(호스트) - 실패(예약 남아있음)")
    void userWithdraw_with_reservation_future_host() throws Exception{
        //given
        Reservation reservation = Reservation.builder()
            .guest(guest)
            .host(host)
            .space(space)
            .status(ReservationStatus.ACCEPTED)
            .usagePurpose(UsagePurpose.BARISTA_TRAINING)
            .requestMessage("테슽뚜")
            .date(LocalDate.now().plusDays(1))
            .startTime(LocalTime.of(12, 0, 0))
            .endTime(LocalTime.of(16, 0, 0))
            .price(60000)
            .guestCount(2)
            .build();
        reservationRepository.save(reservation);

        //when  then
        mockMvc.perform(delete("/api/users")
                .header("Authorization", "Bearer " + hostToken)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(ReservationErrorCode.FUTURE_RESERVATION_REMAIN.getMessage()));

        assertFalse(host.isDeleted());
    }
}
