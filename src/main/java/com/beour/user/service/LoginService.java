package com.beour.user.service;

import com.beour.global.exception.exceptionType.TokenExpiredException;
import com.beour.global.exception.exceptionType.TokenNotFoundException;
import com.beour.global.exception.exceptionType.UserNotFoundException;
import com.beour.global.jwt.JWTUtil;
import com.beour.global.response.ApiResponse;
import com.beour.token.entity.RefreshToken;
import com.beour.token.repository.RefreshTokenRepository;
import com.beour.user.dto.FindLoginIdRequestDto;
import com.beour.user.dto.FindLoginIdResponseDto;
import com.beour.user.dto.ResetPasswordRequestDto;
import com.beour.user.dto.ResetPasswordResponseDto;
import com.beour.user.entity.User;
import com.beour.user.enums.TokenExpireTime;
import com.beour.user.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class LoginService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public FindLoginIdResponseDto findLoginId(FindLoginIdRequestDto dto) {
        User user = userRepository.findByNameAndPhoneAndEmailAndDeletedAtIsNull(dto.getName(), dto.getPhone(),
            dto.getEmail()).orElseThrow(
            () -> new UserNotFoundException("일치하는 회원을 찾을 수 없습니다.")
        );

        return new FindLoginIdResponseDto(user.getLoginId());
    }

    @Transactional
    public ApiResponse<ResetPasswordResponseDto> resetPassword(ResetPasswordRequestDto dto) {
        if (isExistUser(dto)) {
            String tempPassword = generateTempPassword();
            String encode = bCryptPasswordEncoder.encode(tempPassword);
            userRepository.updatePasswordByLoginId(dto.getLoginId(), encode);

            return ApiResponse.ok(new ResetPasswordResponseDto(tempPassword));
        }

        throw new UserNotFoundException("일치하는 회원을 찾을 수 없습니다.");
    }

    private Boolean isExistUser(ResetPasswordRequestDto dto) {
        User userByLoginID = userRepository.findByLoginId(dto.getLoginId()).orElseThrow(
            () -> new UserNotFoundException("일치하는 회원을 찾을 수 없습니다.")
        );

        User userByNamePhoneEmail = userRepository.findByNameAndPhoneAndEmailAndDeletedAtIsNull(dto.getName(),
            dto.getPhone(), dto.getEmail()).orElseThrow(
            () -> new UserNotFoundException("일치하는 회원을 찾을 수 없습니다.")
        );

        checkDeletedUser(userByLoginID);
        checkDeletedUser(userByNamePhoneEmail);

        if (userByLoginID.getId() == userByNamePhoneEmail.getId()) {
            return true;
        }

        throw new UserNotFoundException("일치하는 회원을 찾을 수 없습니다.");
    }

    private static void checkDeletedUser(User user) {
        if (user.isDeleted()) {
            throw new UserNotFoundException("탈퇴한 회원입니다.");
        }
    }

    private String generateTempPassword() {
        int length = 10;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();

        Random random = new SecureRandom();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }

        return sb.toString();
    }

    public String[] reissueRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refresh")) {
                refresh = cookie.getValue();
            }
        }

        if (refresh == null) {
            throw new TokenNotFoundException("refresh 토큰을 찾을 수 없습니다.");
        }

        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException ex) {
            throw new TokenExpiredException("refresh 토큰 만료");
        }

        String category = jwtUtil.getCategory(refresh);
        if (!category.equals("refresh")) {
            throw new TokenNotFoundException("refresh 토큰을 찾을 수 없습니다.");
        }

        Boolean isExistRefresh = refreshTokenRepository.existsByRefresh(refresh);
        if (!isExistRefresh) {
            throw new TokenNotFoundException("refresh 토큰을 찾을 수 없습니다.");
        }

        String loginId = jwtUtil.getLoginId(refresh);
        String role = jwtUtil.getRole(refresh);

        String newAccessToken = "Bearer " + jwtUtil.createJwt("access", loginId, role,
            TokenExpireTime.ACCESS_TOKEN_EXPIRATION_MILLIS.getValue());
        String newRefreshToken = jwtUtil.createJwt("refresh", loginId, role,
            TokenExpireTime.REFRESH_TOKEN_EXPIRATION_MILLIS.getValue());

        refreshTokenRepository.deleteByRefresh(refresh);
        addRefreshToken(loginId, newRefreshToken);

        String[] tokens = new String[2];
        tokens[0] = newAccessToken;
        tokens[1] = newRefreshToken;
        return tokens;
    }

    private void addRefreshToken(String loginId, String newRefreshToken) {
        RefreshToken refreshToken = RefreshToken.builder()
            .loginId(loginId)
            .refresh(newRefreshToken)
            .expiration(new Date(System.currentTimeMillis()
                + TokenExpireTime.REFRESH_TOKEN_EXPIRATION_MILLIS.getValue()).toString())
            .build();
        refreshTokenRepository.save(refreshToken);
    }
}
