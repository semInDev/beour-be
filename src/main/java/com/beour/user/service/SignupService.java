package com.beour.user.service;

import com.beour.user.dto.SignupDto;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SignupService {
    private final UserRepository userRepository;

    public User create(SignupDto signUpDto){
        User user = User.createFrom(signUpDto);

        return userRepository.save(user);
    }
}
