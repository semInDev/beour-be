package com.beour.user.service;

import com.beour.global.exception.exceptionType.InvalidCredentialsException;
import com.beour.global.exception.exceptionType.UserNotFoundException;
import com.beour.user.dto.CustomUserDetails;
import com.beour.user.dto.UserInformationDetailResponseDto;
import com.beour.user.dto.UserInformationSimpleResponseDto;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MyInformationService {

    private final UserRepository userRepository;

    public UserInformationSimpleResponseDto getUserInformationSimple(){
        String userLoginId = findUserLoginIdFromToken();
        User user = userRepository.findByLoginId(userLoginId).orElseThrow(
            () -> new UserNotFoundException("해당 유저를 찾을 수 없습니다.")
        );

        UserInformationSimpleResponseDto dto = new UserInformationSimpleResponseDto();
        dto.setUserName(user.getName());
        dto.setUserEmail(user.getEmail());

        return dto;
    }

    public UserInformationDetailResponseDto getUserInformationDetail(){
        String userLoginId = findUserLoginIdFromToken();
        User user = userRepository.findByLoginId(userLoginId).orElseThrow(
            () -> new UserNotFoundException("해당 유저를 찾을 수 없습니다.")
        );

        UserInformationDetailResponseDto dto = new UserInformationDetailResponseDto();
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setNickName(user.getNickname());
        dto.setPhoneNum(user.getPhone());

        return dto;
    }

    private String findUserLoginIdFromToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()){
            throw new InvalidCredentialsException("인증된 유저가 없습니다.");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }

}
