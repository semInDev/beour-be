package com.beour.user.service;

import com.beour.global.exception.error.errorcode.GlobalErrorCode;
import com.beour.global.exception.error.errorcode.UserErrorCode;
import com.beour.global.exception.exceptionType.InputInvalidFormatException;
import com.beour.global.exception.exceptionType.UserNotFoundException;
import com.beour.user.dto.ChangePasswordRequestDto;
import com.beour.user.dto.UpdateUserInfoRequestDto;
import com.beour.user.dto.UpdateUserInfoResponseDto;
import com.beour.user.dto.UserInformationDetailResponseDto;
import com.beour.user.dto.UserInformationSimpleResponseDto;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MyInformationService {

    private final UserRepository userRepository;
    private final SignupService signupService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Transactional
    public void updatePassword(ChangePasswordRequestDto changePasswordRequestDto){
        User user = findUserFromToken();
        user.updatePassword(bCryptPasswordEncoder.encode(changePasswordRequestDto.getNewPassword()));
    }

    public UserInformationSimpleResponseDto getUserInformationSimple(){
        User user = findUserFromToken();

        return new UserInformationSimpleResponseDto(user.getName(), user.getEmail());
    }

    public UserInformationDetailResponseDto getUserInformationDetail(){
        User user = findUserFromToken();

        return UserInformationDetailResponseDto.builder()
            .name(user.getName())
            .email(user.getEmail())
            .nickName(user.getNickname())
            .phoneNum(user.getPhone())
            .build();
    }

    @Transactional
    public UpdateUserInfoResponseDto updateUserInfo(UpdateUserInfoRequestDto requestDto){
        User user = findUserFromToken();

        if(!requestDto.getNewNickname().isEmpty()){
            signupService.checkNicknameDuplicate(requestDto.getNewNickname());
            user.updateNickname(requestDto.getNewNickname());
        }

        if(!requestDto.getNewPhone().isEmpty()){
            user.updatePhone(requestDto.getNewPhone());
        }

        if(requestDto.getNewNickname().isEmpty() && requestDto.getNewPhone().isEmpty()){
            throw new InputInvalidFormatException(GlobalErrorCode.NO_INFO_TO_UPDATE);
        }

        User updatedUser = findUserFromToken();
        return UpdateUserInfoResponseDto.builder()
            .newNickname(updatedUser.getNickname())
            .newPhone(updatedUser.getPhone())
            .build();
    }

    @Transactional
    public void deleteUser(){
        User user = findUserFromToken();
        user.softDelete();
    }

    private User findUserFromToken() {
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByLoginIdAndDeletedAtIsNull(loginId).orElseThrow(
            () -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND)
        );
    }

}
