package com.beour.reservation.guest.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.beour.global.jwt.JWTUtil;
import com.beour.reservation.commons.entity.Reservation;
import com.beour.reservation.commons.enums.ReservationStatus;
import com.beour.reservation.commons.enums.UsagePurpose;
import com.beour.reservation.commons.repository.ReservationRepository;
import com.beour.space.domain.entity.AvailableTime;
import com.beour.space.domain.entity.Space;
import com.beour.space.domain.repository.AvailableTimeRepository;
import com.beour.space.domain.repository.SpaceRepository;
import com.beour.space.host.enums.SpaceCategory;
import com.beour.space.host.enums.UseCategory;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ReservationGuestControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JWTUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SpaceRepository spaceRepository;
    @Autowired
    private AvailableTimeRepository availableTimeRepository;
    @Autowired
    private ReservationRepository reservationRepository;


    private User guest;
    private User host;
    private Space space;
    private String accessToken;
    private AvailableTime availableTimePast;
    private AvailableTime availableTimeCurrent;
    private AvailableTime availableTimeNext;

    @BeforeEach
    void setUp() {
        guest = User.builder()
            .loginId("guest")
            .password(passwordEncoder.encode("guestpassword!"))
            .name("게스트")
            .nickname("guest")
            .email("guest@gmail.com")
            .phone("01012345678")
            .role("GUEST")
            .build();
        userRepository.save(guest);

        host = User.builder()
            .loginId("host1")
            .password(passwordEncoder.encode("host1password!"))
            .name("호스트1")
            .nickname("host1")
            .email("host1@gmail.com")
            .phone("01012345678")
            .role("HOST")
            .build();
        userRepository.save(host);

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

        availableTimePast = AvailableTime.builder()
            .space(space)
            .date(LocalDate.now().minusDays(1))
            .startTime(LocalTime.of(1, 0, 0))
            .endTime(LocalTime.of(23, 0, 0))
            .build();
        availableTimeRepository.save(availableTimePast);
        space.getAvailableTimes().add(availableTimePast);

        availableTimeCurrent = AvailableTime.builder()
            .space(space)
            .date(LocalDate.now())
            .startTime(LocalTime.of(1, 0, 0))
            .endTime(LocalTime.of(23, 0, 0))
            .build();
        availableTimeRepository.save(availableTimeCurrent);
        space.getAvailableTimes().add(availableTimeCurrent);

        availableTimeNext = AvailableTime.builder()
            .space(space)
            .date(LocalDate.now().plusDays(1))
            .startTime(LocalTime.of(1, 0, 0))
            .endTime(LocalTime.of(23, 0, 0))
            .build();
        availableTimeRepository.save(availableTimeNext);
        space.getAvailableTimes().add(availableTimeNext);

        accessToken = jwtUtil.createJwt(
            "access",
            guest.getLoginId(),
            "ROLE_" + guest.getRole(),
            1000L * 60 * 30    // 30분
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        reservationRepository.deleteAll();
        availableTimeRepository.deleteAll();
        spaceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("예약 등록 - 인원 초과")
    void create_reservation_exceed_people_num() throws Exception {
        //given
        String requestJson = String.format("""
            {
                "hostId": %d,
                "spaceId": %d,
                "date": "%s",
                "startTime": "13:00:00",
                "endTime": "14:00:00",
                "price": 15000,
                "guestCount": 4,
                "usagePurpose": "BARISTA_TRAINING",
                "requestMessage": "요청 사항 테스트"
            }
            """, host.getId(), space.getId(), LocalDate.now().plusDays(1));

        //when  then
        mockMvc.perform(post("/api/spaces/reserve")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("해당 인원은 예약이 불가합니다."));
    }

    @Test
    @DisplayName("예약 등록 - 시간당 가격과 총 가격이 불일치할 경우")
    void create_reservation_incorrect_price() throws Exception {
        //given
        String requestJson = String.format("""
            {
                "hostId": %d,
                "spaceId": %d,
                "date": "%s",
                "startTime": "13:00:00",
                "endTime": "14:00:00",
                "price": 30000,
                "guestCount": 2,
                "usagePurpose": "BARISTA_TRAINING",
                "requestMessage": "요청 사항 테스트"
            }
            """, host.getId(), space.getId(), LocalDate.now().plusDays(1));

        //when  then
        mockMvc.perform(post("/api/spaces/reserve")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("해당 가격이 맞지 않습니다."));
    }

    @Test
    @DisplayName("예약 등록 - 예약 날짜가 과거 날짜일 경우")
    void create_reservation_date_past() throws Exception {
        //given
        String requestJson = String.format("""
            {
                "hostId": %d,
                "spaceId": %d,
                "date": "%s",
                "startTime": "13:00:00",
                "endTime": "14:00:00",
                "price": 15000,
                "guestCount": 2,
                "usagePurpose": "BARISTA_TRAINING",
                "requestMessage": "요청 사항 테스트"
            }
            """, host.getId(), space.getId(), LocalDate.now().minusDays(1));

        //when  then
        mockMvc.perform(post("/api/spaces/reserve")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("예약 가능한 시간이 없습니다."));
    }

    @Test
    @DisplayName("예약 등록 - 예약 시간이 과거 시간일 경우")
    void create_reservation_past_time() throws Exception {
        //given
        int currentHour = LocalTime.now().getHour();
        String requestJson = String.format("""
            {
                "hostId": %d,
                "spaceId": %d,
                "date": "%s",
                "startTime": "%s",
                "endTime": "%s",
                "price": 30000,
                "guestCount": 2,
                "usagePurpose": "BARISTA_TRAINING",
                "requestMessage": "요청 사항 테스트"
            }
            """, host.getId(), space.getId(), LocalDate.now(), LocalTime.of(currentHour - 1, 0, 0), LocalTime.of(currentHour + 1, 0, 0));

        //when  then
        mockMvc.perform(post("/api/spaces/reserve")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("예약 가능한 시간이 존재하지 않습니다."));
    }

    @Test
    @DisplayName("예약 등록 - 예약 날짜 및 시간이 다른 예약과 겹칠 경우")
    void create_reservation_conflict_other_reservation() throws Exception {
        //given
        Reservation reservationPast = Reservation.builder()
            .guest(guest)
            .host(host)
            .space(space)
            .status(ReservationStatus.COMPLETED)
            .usagePurpose(UsagePurpose.BARISTA_TRAINING)
            .requestMessage("테슽뚜")
            .date(LocalDate.now().plusDays(1))
            .startTime(LocalTime.of(12, 0, 0))
            .endTime(LocalTime.of(16, 0, 0))
            .price(60000)
            .guestCount(2)
            .build();
        reservationRepository.save(reservationPast);

        String requestJson = String.format("""
            {
                "hostId": %d,
                "spaceId": %d,
                "date": "%s",
                "startTime": "15:00:00",
                "endTime": "17:00:00",
                "price": 30000,
                "guestCount": 2,
                "usagePurpose": "BARISTA_TRAINING",
                "requestMessage": "요청 사항 테스트"
            }
            """, host.getId(), space.getId(), LocalDate.now().plusDays(1));

        //when  then
        mockMvc.perform(post("/api/spaces/reserve")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("예약이 불가능한 시간입니다."));
    }

    @Test
    @DisplayName("예약 등록 - 존재하지 않는 호스트")
    void create_reservation_not_fount_host() throws Exception {
        //given
        String requestJson = String.format("""
            {
                "hostId": 100,
                "spaceId": %d,
                "date": "%s",
                "startTime": "15:00:00",
                "endTime": "17:00:00",
                "price": 30000,
                "guestCount": 2,
                "usagePurpose": "BARISTA_TRAINING",
                "requestMessage": "요청 사항 테스트"
            }
            """, space.getId(), LocalDate.now().plusDays(1));

        //when  then
        mockMvc.perform(post("/api/spaces/reserve")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("존재하지 않는 유저입니다."));
    }

    @Test
    @DisplayName("예약 등록 - 존재하지 않는 공간")
    void create_reservation_not_fount_space() throws Exception {
        //given
        String requestJson = String.format("""
            {
                "hostId": %d,
                "spaceId": 100,
                "date": "%s",
                "startTime": "15:00:00",
                "endTime": "17:00:00",
                "price": 30000,
                "guestCount": 2,
                "usagePurpose": "BARISTA_TRAINING",
                "requestMessage": "요청 사항 테스트"
            }
            """, host.getId(), LocalDate.now().plusDays(1));

        //when  then
        mockMvc.perform(post("/api/spaces/reserve")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("존재하지 않는 공간입니다."));
    }

    @Test
    @DisplayName("예약 등록 - 성공")
    void success_create_reservation() throws Exception {
        //given
        String requestJson = String.format("""
            {
                "hostId": %d,
                "spaceId": %d,
                "date": "%s",
                "startTime": "15:00:00",
                "endTime": "17:00:00",
                "price": 30000,
                "guestCount": 2,
                "usagePurpose": "BARISTA_TRAINING",
                "requestMessage": "요청 사항 테스트"
            }
            """, host.getId(), space.getId(), LocalDate.now().plusDays(1));

        //when  then
        mockMvc.perform(post("/api/spaces/reserve")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isOk());
    }

    /**
     * 예약 현황 조회
     * - 성공
     * - 과거 예약 잘 걸러지는지
     * - 현 시점의 시간 이전의 시간 잘 걸러지는지
     * - 예약 없을 경우
     *
     * 지난 예약 조회
     * - 예약 없을 경우
     * - 시간 지나면 예약 사용 완료 상태로 변경
     *
     * 예약 취소
     * - 성공
     * - 존재하지 않는 예약일 경우
     * - 이미 확정된 예약일 경우
     */
}