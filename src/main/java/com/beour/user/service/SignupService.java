package com.beour.user.service;

import com.beour.global.exception.SignupException;
import com.beour.user.dto.CheckDuplicateNickNameDto;
import com.beour.user.dto.SignupDto;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SignupService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public User create(SignupDto dto){
        if(userRepository.existsByLoginId(dto.getLoginId())){
            throw new SignupException("이미 사용중인 아이디입니다.");
        }

        if(userRepository.existsByNickname(dto.getNickname())){
            throw new SignupException("이미 사용중인 닉네임입니다.");
        }

        dto.setPassword(bCryptPasswordEncoder.encode(dto.getPassword()));
        User user = User.createFrom(dto);
        return userRepository.save(user);
    }

    public boolean checkLoginIdDuplicate(String loginId){
        return userRepository.existsByLoginId(loginId);
    }

    public boolean checkNicknameDuplicate(String nickname){
        return userRepository.existsByNickname(nickname);
    }
}
