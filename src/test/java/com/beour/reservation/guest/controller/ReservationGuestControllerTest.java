package com.beour.reservation.guest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.contains;

import com.beour.global.jwt.JWTUtil;
import com.beour.reservation.commons.entity.Reservation;
import com.beour.reservation.commons.enums.ReservationStatus;
import com.beour.reservation.commons.enums.UsagePurpose;
import com.beour.reservation.commons.repository.ReservationRepository;
import com.beour.space.domain.entity.AvailableTime;
import com.beour.space.domain.entity.Space;
import com.beour.space.domain.enums.SpaceCategory;
import com.beour.space.domain.enums.UseCategory;
import com.beour.space.domain.repository.AvailableTimeRepository;
import com.beour.space.domain.repository.SpaceRepository;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
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
import org.springframework.transaction.annotation.Transactional;

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

    @Test
    @DisplayName("이용 가능한 시간 조회 - 과거 날짜로 조회")
    void check_available_time_with_past_date() throws Exception {
        //given
        String requestJson = String.format("""
            {
                "spaceId": %d,
                "date": "%s"
            }
            """, space.getId(), LocalDate.now().minusDays(1));

        //when  then
        mockMvc.perform(post("/api/spaces/reserve/available-times")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("예약 가능한 시간이 없습니다."));
    }

    @Test
    @DisplayName("이용 가능한 시간 조회 - 오늘 날짜로 조회")
    void check_available_time_with_today() throws Exception {
        //given
        int currentHour = LocalTime.now().getHour();
        String requestJson = String.format("""
            {
                "spaceId": %d,
                "date": "%s"
            }
            """, space.getId(), LocalDate.now());
        List<String> availableTimes = new ArrayList<>();
        for(int i = currentHour + 1; i <= 22; i++){
            availableTimes.add(String.format("%02d:00:00", i));
        }

        //when  then
        mockMvc.perform(post("/api/spaces/reserve/available-times")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.timeList.length()").value(availableTimes.size()))
            .andExpect(jsonPath("$.data.timeList").value(contains(availableTimes.toArray())));
    }

    @Test
    @DisplayName("이용 가능한 시간 조회 - 가능한 시간 없을 경우")
    void check_available_time_not_found() throws Exception {
        //given
        String requestJson = String.format("""
            {
                "spaceId": %d,
                "date": "%s"
            }
            """, space.getId(), LocalDate.now().plusDays(3));

        //when  then
        mockMvc.perform(post("/api/spaces/reserve/available-times")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("예약 가능한 시간이 없습니다."));
    }

    @Test
    @DisplayName("이용 가능한 시간 조회 - 해당 날에 예약이 있을 경우")
    void check_available_time_has_other_reservation() throws Exception {
        //given
        Reservation reservationPast = Reservation.builder()
            .guest(guest)
            .host(host)
            .space(space)
            .status(ReservationStatus.COMPLETED)
            .usagePurpose(UsagePurpose.BARISTA_TRAINING)
            .requestMessage("테슽뚜")
            .date(LocalDate.now().plusDays(1))
            .startTime(LocalTime.of(1, 0, 0))
            .endTime(LocalTime.of(5, 0, 0))
            .price(60000)
            .guestCount(2)
            .build();
        reservationRepository.save(reservationPast);

        String requestJson = String.format("""
            {
                "spaceId": %d,
                "date": "%s"
            }
            """, space.getId(), LocalDate.now().plusDays(1));

        List<String> availableTimes = new ArrayList<>();
        for(int i = 5; i <= 22; i++){
            availableTimes.add(String.format("%02d:00:00", i));
        }

        //when  then
        mockMvc.perform(post("/api/spaces/reserve/available-times")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.timeList.length()").value(availableTimes.size()))
            .andExpect(jsonPath("$.data.timeList").value(contains(availableTimes.toArray())));
    }

    @Test
    @Transactional
    @DisplayName("이용 가능한 시간 조회 - 예약 거절된 시간 조회")
    void check_available_time_has_other_rejected_reservation() throws Exception {
        //given
        Reservation reservation = Reservation.builder()
            .guest(guest)
            .host(host)
            .space(space)
            .status(ReservationStatus.COMPLETED)
            .usagePurpose(UsagePurpose.BARISTA_TRAINING)
            .requestMessage("테슽뚜")
            .date(LocalDate.now().plusDays(1))
            .startTime(LocalTime.of(1, 0, 0))
            .endTime(LocalTime.of(5, 0, 0))
            .price(60000)
            .guestCount(2)
            .build();
        reservationRepository.save(reservation);
        reservation.updateStatus(ReservationStatus.REJECTED);

        String requestJson = String.format("""
            {
                "spaceId": %d,
                "date": "%s"
            }
            """, space.getId(), LocalDate.now().plusDays(1));

        List<String> availableTimes = new ArrayList<>();
        for(int i = 1; i <= 22; i++){
            availableTimes.add(String.format("%02d:00:00", i));
        }

        //when  then
        mockMvc.perform(post("/api/spaces/reserve/available-times")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.timeList.length()").value(availableTimes.size()))
            .andExpect(jsonPath("$.data.timeList").value(contains(availableTimes.toArray())));
    }

    @Test
    @DisplayName("예약 현황 조회 - 현 시점의 시간 이전의 시간 잘 걸러지는지")
    void check_reservation_list_filtering_past_reservation() throws Exception {
        //given
        int currentHour = LocalTime.now().getHour();
        Reservation reservationPast = Reservation.builder()
            .guest(guest)
            .host(host)
            .space(space)
            .status(ReservationStatus.COMPLETED)
            .usagePurpose(UsagePurpose.BARISTA_TRAINING)
            .requestMessage("테슽뚜")
            .date(LocalDate.now())
            .startTime(LocalTime.of(currentHour -1, 0, 0))
            .endTime(LocalTime.of(currentHour, 0, 0))
            .price(15000)
            .guestCount(2)
            .build();
        reservationRepository.save(reservationPast);
        Reservation reservationFuture = Reservation.builder()
            .guest(guest)
            .host(host)
            .space(space)
            .status(ReservationStatus.ACCEPTED)
            .usagePurpose(UsagePurpose.BARISTA_TRAINING)
            .requestMessage("테슽뚜")
            .date(LocalDate.now())
            .startTime(LocalTime.of(currentHour + 1, 0, 0))
            .endTime(LocalTime.of(currentHour + 2, 0, 0))
            .price(15000)
            .guestCount(2)
            .build();
        reservationRepository.save(reservationFuture);

        //when  then
        mockMvc.perform(get("/api/reservation")
                .header("Authorization", "Bearer " + accessToken)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].spaceName").value(reservationFuture.getSpace().getName()))
            .andExpect(jsonPath("$.data[0].startTime").value(String.format("%02d:00:00", reservationFuture.getStartTime().getHour())))
            .andExpect(jsonPath("$.data[0].endTime").value(String.format("%02d:00:00", reservationFuture.getEndTime().getHour())));
    }

    @Test
    @DisplayName("예약 현황 조회 - 예약 없을 경우")
    void check_reservation_list_not_found() throws Exception {
        //when  then
        mockMvc.perform(get("/api/reservation")
                .header("Authorization", "Bearer " + accessToken)
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("예약이 없습니다."));
    }

    @Test
    @DisplayName("지난 예약 현황 조회 - 예약 없을 경우")
    void check_past_reservation_list_not_found() throws Exception {
        //when  then
        mockMvc.perform(get("/api/reservation/past")
                .header("Authorization", "Bearer " + accessToken)
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("예약이 없습니다."));
    }

    @Test
    @DisplayName("지난 예약 현황 조회 - 시간 지나면 예약 사용 완료 상태로 변경")
    void check_past_reservation_list_use_complete() throws Exception {
        //given
        Reservation reservationPast = Reservation.builder()
            .guest(guest)
            .host(host)
            .space(space)
            .status(ReservationStatus.ACCEPTED)
            .usagePurpose(UsagePurpose.BARISTA_TRAINING)
            .requestMessage("테슽뚜")
            .date(LocalDate.now().minusDays(1))
            .startTime(LocalTime.of(13, 0, 0))
            .endTime(LocalTime.of(14, 0, 0))
            .price(15000)
            .guestCount(2)
            .build();
        reservationRepository.save(reservationPast);

        //when  then
        mockMvc.perform(get("/api/reservation/past")
                .header("Authorization", "Bearer " + accessToken)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].status").value("COMPLETED"));
    }

    @Test
    @DisplayName("예약 취소 - 존재하지 않는 에약일 경우")
    void cancel_reservation_not_found() throws Exception {
        //when  then
        mockMvc.perform(delete("/api/reservation/cancel")
                .param("reservationId", "100")
                .header("Authorization", "Bearer " + accessToken)
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("해당 예약이 존재하지 않습니다."));
    }

    @Test
    @DisplayName("예약 취소 - 이미 확정된 예약일 경우")
    void cancel_reservation_status_accepted() throws Exception {
        //given
        Reservation reservation = Reservation.builder()
            .guest(guest)
            .host(host)
            .space(space)
            .status(ReservationStatus.ACCEPTED)
            .usagePurpose(UsagePurpose.BARISTA_TRAINING)
            .requestMessage("테슽뚜")
            .date(LocalDate.now().plusDays(1))
            .startTime(LocalTime.of(13, 0, 0))
            .endTime(LocalTime.of(14, 0, 0))
            .price(15000)
            .guestCount(2)
            .build();
        reservationRepository.save(reservation);

        //when  then
        mockMvc.perform(delete("/api/reservation/cancel")
                .param("reservationId", String.format("%d", reservation.getId()))
                .header("Authorization", "Bearer " + accessToken)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("해당 예약은 취소할 수 없습니다."));
    }

    @Test
    @Transactional
    @DisplayName("예약 취소 - 성공")
    void success_cancel_reservation() throws Exception {
        //given
        Reservation reservation = Reservation.builder()
            .guest(guest)
            .host(host)
            .space(space)
            .status(ReservationStatus.PENDING)
            .usagePurpose(UsagePurpose.BARISTA_TRAINING)
            .requestMessage("테슽뚜")
            .date(LocalDate.now().plusDays(1))
            .startTime(LocalTime.of(13, 0, 0))
            .endTime(LocalTime.of(14, 0, 0))
            .price(15000)
            .guestCount(2)
            .build();
        reservationRepository.save(reservation);

        //when  then
        mockMvc.perform(delete("/api/reservation/cancel")
                .param("reservationId", String.format("%d", reservation.getId()))
                .header("Authorization", "Bearer " + accessToken)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value("예약이 성공적으로 취소되었습니다."));

        assertEquals(ReservationStatus.REJECTED, reservation.getStatus());
    }
}