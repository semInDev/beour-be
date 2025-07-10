package com.beour.user.service;

import com.beour.global.exception.error.errorcode.UserErrorCode;
import com.beour.global.exception.exceptionType.DuplicateUserInfoException;
import com.beour.user.dto.SignupRequestDto;
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

    public User create(SignupRequestDto dto) {
        checkValidUser(dto);
        dto.encodingPassword(bCryptPasswordEncoder.encode(dto.getPassword()));
        User user = User.createFrom(dto);

        return userRepository.save(user);
    }

    private void checkValidUser(SignupRequestDto dto) {
        checkDuplicateWithAdminId(dto.getLoginId());
        checkLoginIdDuplicate(dto.getLoginId());
        checkNicknameDuplicate(dto.getNickname());
    }

    private static void checkDuplicateWithAdminId(String loginId) {
        if (loginId.equals("admin")) {
            throw new DuplicateUserInfoException(UserErrorCode.LOGIN_ID_DUPLICATE);
        }
    }

    public boolean checkLoginIdDuplicate(String loginId) {
        Boolean isUserExist = userRepository.existsByLoginIdAndDeletedAtIsNull(loginId);

        if (isUserExist) {
            throw new DuplicateUserInfoException(UserErrorCode.LOGIN_ID_DUPLICATE);
        }

        return false;
    }

    public boolean checkNicknameDuplicate(String nickname) {
        Boolean isUserExist = userRepository.existsByNicknameAndDeletedAtIsNull(nickname);

        if (isUserExist) {
            throw new DuplicateUserInfoException(UserErrorCode.NICKNAME_ID_DUPLICATE);
        }

        return false;
    }

}
