package com.beour.reservation.host.service;

import com.beour.global.exception.exceptionType.UserNotFoundException;
import com.beour.reservation.host.dto.HostSpaceListResponseDto;
import com.beour.space.domain.entity.Space;
import com.beour.space.domain.repository.SpaceRepository;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ReservationHostService {

    private final SpaceRepository spaceRepository;
    private final UserRepository userRepository;

    public List<HostSpaceListResponseDto> getHostSpaces(Long hostId) {
        User host = userRepository.findById(hostId).orElseThrow(
                () -> new UserNotFoundException("존재하지 않는 호스트입니다.")
        );

        List<Space> spaceList = spaceRepository.findByHostAndDeletedAtIsNull(host);

        if (spaceList.isEmpty()) {
            throw new RuntimeException("해당 호스트가 등록한 공간이 없습니다.");
        }

        List<HostSpaceListResponseDto> responseDtoList = new ArrayList<>();
        for (Space space : spaceList) {
            responseDtoList.add(HostSpaceListResponseDto.of(space));
        }

        return responseDtoList;
    }
}
