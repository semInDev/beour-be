package com.beour.user.service;

import com.beour.global.exception.error.errorcode.GlobalErrorCode;
import com.beour.global.exception.error.errorcode.ReservationErrorCode;
import com.beour.global.exception.error.errorcode.UserErrorCode;
import com.beour.global.exception.exceptionType.InputInvalidFormatException;
import com.beour.global.exception.exceptionType.UserNotFoundException;
import com.beour.global.exception.exceptionType.UserWithdrawException;
import com.beour.reservation.commons.entity.Reservation;
import com.beour.reservation.commons.enums.ReservationStatus;
import com.beour.reservation.commons.repository.ReservationRepository;
import com.beour.space.domain.entity.Space;
import com.beour.space.domain.repository.SpaceRepository;
import com.beour.user.dto.ChangePasswordRequestDto;
import com.beour.user.dto.UpdateUserInfoRequestDto;
import com.beour.user.dto.UpdateUserInfoResponseDto;
import com.beour.user.dto.UserInformationDetailResponseDto;
import com.beour.user.dto.UserInformationSimpleResponseDto;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MyInformationService {

    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final SpaceRepository spaceRepository;
    private final SignupService signupService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Transactional
    public void updatePassword(ChangePasswordRequestDto changePasswordRequestDto) {
        User user = findUserFromToken();
        user.updatePassword(
            bCryptPasswordEncoder.encode(changePasswordRequestDto.getNewPassword()));
    }

    public UserInformationSimpleResponseDto getUserInformationSimple() {
        User user = findUserFromToken();

        return new UserInformationSimpleResponseDto(user.getName(), user.getLoginId());
    }

    public UserInformationDetailResponseDto getUserInformationDetail() {
        User user = findUserFromToken();

        return UserInformationDetailResponseDto.builder()
            .name(user.getName())
            .email(user.getEmail())
            .nickName(user.getNickname())
            .phoneNum(user.getPhone())
            .build();
    }

    @Transactional
    public UpdateUserInfoResponseDto updateUserInfo(UpdateUserInfoRequestDto requestDto) {
        User user = findUserFromToken();

        if (!requestDto.getNewNickname().isEmpty()) {
            signupService.checkNicknameDuplicate(requestDto.getNewNickname());
            user.updateNickname(requestDto.getNewNickname());
        }

        if (!requestDto.getNewPhone().isEmpty()) {
            user.updatePhone(requestDto.getNewPhone());
        }

        if (requestDto.getNewNickname().isEmpty() && requestDto.getNewPhone().isEmpty()) {
            throw new InputInvalidFormatException(GlobalErrorCode.NO_INFO_TO_UPDATE);
        }

        User updatedUser = findUserFromToken();
        return UpdateUserInfoResponseDto.builder()
            .newNickname(updatedUser.getNickname())
            .newPhone(updatedUser.getPhone())
            .build();
    }

    @Transactional
    public void deleteUser() {
        User user = findUserFromToken();
        checkWithDrawable(user);

        user.softDelete();
    }

    private void checkWithDrawable(User user) {
        if(user.getRole().equals("HOST")){
            checkHostWithDrawable(user);
        }

        if(user.getRole().equals("GUEST")){
            checkGuestWithDrawable(user);
        }
    }

    private void checkHostWithDrawable(User host) {
        List<Reservation> reservations = reservationRepository.findByHostIdAndStatusInAndDeletedAtIsNull(
            host.getId(), List.of(ReservationStatus.PENDING, ReservationStatus.ACCEPTED));

        if(!reservations.isEmpty()){
            throw new UserWithdrawException(ReservationErrorCode.FUTURE_RESERVATION_REMAIN);
        }

        List<Space> spaces = spaceRepository.findByHostAndDeletedAtIsNull(host);
        spaces.forEach(Space::softDelete);
    }

    private void checkGuestWithDrawable(User guest) {
        List<Reservation> reservations = reservationRepository.findByGuestIdAndStatusInAndDeletedAtIsNull(
            guest.getId(), List.of(ReservationStatus.PENDING, ReservationStatus.ACCEPTED));

        if(!reservations.isEmpty()){
            throw new UserWithdrawException(ReservationErrorCode.FUTURE_RESERVATION_REMAIN);
        }
    }

    private User findUserFromToken() {
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByLoginIdAndDeletedAtIsNull(loginId).orElseThrow(
            () -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND)
        );
    }

}
