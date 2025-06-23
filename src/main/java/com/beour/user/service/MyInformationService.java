package com.beour.user.service;

import com.beour.global.exception.exceptionType.InvalidFormatException;
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

        String newPassword = bCryptPasswordEncoder.encode(changePasswordRequestDto.getNewPassword());
        user.updatePassword(newPassword);
    }

    public UserInformationSimpleResponseDto getUserInformationSimple(){
        User user = findUserFromToken();

        UserInformationSimpleResponseDto dto = new UserInformationSimpleResponseDto(); // 나중에 빌더로 수정해도 좋을 것 같아요. 그리고 UserInformationSimpleResponseDto에 Setter 뺴고
        dto.setUserName(user.getName());
        dto.setUserEmail(user.getEmail());

        return dto;
    }

    public UserInformationDetailResponseDto getUserInformationDetail(){
        User user = findUserFromToken();

        UserInformationDetailResponseDto dto = new UserInformationDetailResponseDto(); // 나중에 빌더로 수정해도 좋을 것 같아요. 그리고 UserInformationDetailResponseDto에 Setter 뺴고
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setNickName(user.getNickname());
        dto.setPhoneNum(user.getPhone());

        return dto;
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
            throw new InvalidFormatException("수정할 정보를 입력해주세요.");
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
            () -> new UserNotFoundException("해당 유저를 찾을 수 없습니다.")
        );
    }

}
