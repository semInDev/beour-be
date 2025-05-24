package com.beour.user.service;

import com.beour.global.exception.exceptionType.DuplicateUserInfoException;
import com.beour.global.exception.exceptionType.InvalidCredentialsException;
import com.beour.global.exception.exceptionType.InvalidFormatException;
import com.beour.user.dto.SignupRequestDto;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.MethodArgumentNotValidException;

@RequiredArgsConstructor
@Service
public class SignupService {

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  //todo: 관리자 계정 만들면 관리자 아이디 체크 로직 추가
  public User create(SignupRequestDto dto) {
    validDuplicateUser(dto);
    checkRoleInvalidRequest(dto.getRole());
    dto.setPassword(bCryptPasswordEncoder.encode(dto.getPassword()));
    User user = User.createFrom(dto);

    return userRepository.save(user);
  }

  private void validDuplicateUser(SignupRequestDto dto) {
    if (userRepository.existsByLoginId(dto.getLoginId())) {
      throw new DuplicateUserInfoException("이미 사용중인 아이디입니다.");
    }

    if (userRepository.existsByNickname(dto.getNickname())) {
      throw new DuplicateUserInfoException("이미 사용중인 닉네임입니다.");
    }
  }

  private void checkRoleInvalidRequest(String role){
    if(!role.equals("HOST") && !role.equals("GUEST")){
      throw new InvalidFormatException("HOST 또는 GUEST만 입력해주세요.");
    }
  }

  public boolean checkLoginIdDuplicate(String loginId) {
    return userRepository.existsByLoginId(loginId);
  }

  public boolean checkNicknameDuplicate(String nickname) {
    return userRepository.existsByNickname(nickname);
  }
}
