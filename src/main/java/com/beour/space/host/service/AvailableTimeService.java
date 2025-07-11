package com.beour.space.host.service;

import com.beour.global.exception.error.errorcode.SpaceErrorCode;
import com.beour.global.exception.error.errorcode.UserErrorCode;
import com.beour.global.exception.exceptionType.SpaceNotFoundException;
import com.beour.global.exception.exceptionType.UnauthorityException;
import com.beour.global.exception.exceptionType.UserNotFoundException;
import com.beour.reservation.commons.entity.Reservation;
import com.beour.reservation.commons.enums.ReservationStatus;
import com.beour.reservation.commons.repository.ReservationRepository;
import com.beour.space.domain.entity.AvailableTime;
import com.beour.space.domain.entity.Space;
import com.beour.space.domain.repository.AvailableTimeRepository;
import com.beour.space.domain.repository.SpaceRepository;
import com.beour.space.host.dto.AvailableTimeDetailResponseDto;
import com.beour.space.host.dto.AvailableTimeUpdateRequestDto;
import com.beour.space.host.dto.HostSpaceListResponseDto;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AvailableTimeService {

    private final SpaceRepository spaceRepository;
    private final UserRepository userRepository;
    private final AvailableTimeRepository availableTimeRepository;
    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public List<HostSpaceListResponseDto> getHostSpaces() {
        User host = findUserFromToken();

        List<Space> spaceList = spaceRepository.findByHostAndDeletedAtIsNull(host);

        if (spaceList.isEmpty()) {
            throw new SpaceNotFoundException(SpaceErrorCode.NO_HOST_SPACE);
        }

        return spaceList.stream()
                .map(space -> HostSpaceListResponseDto.builder()
                        .spaceId(space.getId())
                        .name(space.getName())
                        .address(space.getAddress())
                        .maxCapacity(space.getMaxCapacity())
                        .avgRating(space.getAvgRating())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AvailableTimeDetailResponseDto getAvailableTimeDetail(Long spaceId) {
        User host = findUserFromToken();
        Space space = findSpaceByIdAndValidateOwner(spaceId, host);

        // 수정 가능한 시간들 (AvailableTime 데이터에서 소프트 삭제되지 않은 것들)
        List<AvailableTimeDetailResponseDto.TimeSlot> editableTimeSlots = space.getAvailableTimes().stream()
                .filter(availableTime -> availableTime.getDeletedAt() == null)
                .map(availableTime -> AvailableTimeDetailResponseDto.TimeSlot.builder()
                        .date(availableTime.getDate())
                        .startTime(availableTime.getStartTime())
                        .endTime(availableTime.getEndTime())
                        .build())
                .collect(Collectors.toList());

        // 수정 불가능한 시간들 (예약 상태가 PENDING 또는 ACCEPTED인 것들)
        List<Reservation> reservedSlots = reservationRepository.findBySpaceIdAndDateAndDeletedAtIsNull(spaceId, null)
                .stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.PENDING
                        || reservation.getStatus() == ReservationStatus.ACCEPTED)
                .collect(Collectors.toList());

        List<AvailableTimeDetailResponseDto.TimeSlot> nonEditableTimeSlots = reservedSlots.stream()
                .map(reservation -> AvailableTimeDetailResponseDto.TimeSlot.builder()
                        .date(reservation.getDate())
                        .startTime(reservation.getStartTime())
                        .endTime(reservation.getEndTime())
                        .build())
                .collect(Collectors.toList());

        return AvailableTimeDetailResponseDto.builder()
                .spaceId(spaceId)
                .editableTimeSlots(editableTimeSlots)
                .nonEditableTimeSlots(nonEditableTimeSlots)
                .build();
    }

    @Transactional
    public void updateAvailableTimes(Long spaceId, AvailableTimeUpdateRequestDto requestDto) {
        User host = findUserFromToken();
        Space space = findSpaceByIdAndValidateOwner(spaceId, host);

        // 기존 AvailableTime 데이터 삭제 (소프트 삭제가 아닌 물리적 삭제)
        availableTimeRepository.deleteBySpace(space);

        // 새로운 AvailableTime 데이터 저장
        List<AvailableTime> newAvailableTimes = requestDto.getAvailableTimes().stream()
                .map(timeSlot -> AvailableTime.builder()
                        .space(space)
                        .date(timeSlot.getDate())
                        .startTime(timeSlot.getStartTime())
                        .endTime(timeSlot.getEndTime())
                        .build())
                .collect(Collectors.toList());

        availableTimeRepository.saveAll(newAvailableTimes);
    }

    private User findUserFromToken() {
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByLoginIdAndDeletedAtIsNull(loginId).orElseThrow(
                () -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND)
        );
    }

    private Space findSpaceByIdAndValidateOwner(Long spaceId, User host) {
        Space space = spaceRepository.findById(spaceId).orElseThrow(
                () -> new SpaceNotFoundException(SpaceErrorCode.SPACE_NOT_FOUND)
        );

        // 호스트가 해당 공간의 소유자인지 확인
        if (!space.getHost().getId().equals(host.getId())) {
            throw new UnauthorityException(SpaceErrorCode.NO_PERMISSION);
        }

        return space;
    }
}
