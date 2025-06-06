package com.beour.global.jwt;

import com.beour.global.exception.exceptionType.LoginUserMismatchRole;
import com.beour.global.exception.exceptionType.LoginUserNotFoundException;
import com.beour.global.exception.exceptionType.UserNotFoundException;
import com.beour.user.dto.CustomUserDetails;
import com.beour.user.dto.LoginDto;
import com.beour.user.entity.User;
import com.beour.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;

    private static final long TOKEN_EXPIRATION_MILLIS = 60 * 60 * 10L * 1000;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
        HttpServletResponse response) {

        ObjectMapper objectMapper = new ObjectMapper();
        LoginDto loginDto = null;

        try {
            loginDto = objectMapper.readValue(request.getInputStream(), LoginDto.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            loginDto.getLoginId(), loginDto.getPassword(), null);

        User user = userRepository.findByLoginId(loginDto.getLoginId()).orElse(null);
        if (user == null || user.isDeleted()) {
            throw new LoginUserNotFoundException("존재하지 않는 사용자입니다.");
        }

        if (!user.getRole().equals(loginDto.getRole())) {
            throw new LoginUserMismatchRole("역할이 일치하지 않습니다.");
        }

        return authenticationManager.authenticate(authToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
        HttpServletResponse response,
        FilterChain chain, Authentication authentication)
        throws IOException {

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        String loginId = customUserDetails.getUsername();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        String token = jwtUtil.createJwt(loginId, "ROLE_" + role, TOKEN_EXPIRATION_MILLIS);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Authorization", "Bearer " + token);

        Long userId = customUserDetails.getUserId();
        String jsonResponse = String.format("""
            {
                "code": 200,
                "message": "로그인 성공",
                "userId": %d,
                "loginId": "%s",
                "role": "%s",
                "token": "Bearer %s"
            }
            """, userId, loginId, role, token);

        response.getWriter().write(jsonResponse);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
        HttpServletResponse response, AuthenticationException failed)
        throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String message;
        if (failed instanceof LoginUserNotFoundException) {
            message = "존재하지 않는 사용자입니다.";
        } else if (failed instanceof LoginUserMismatchRole) {
            message = "역할이 일치하지 않습니다.";
        } else if (failed instanceof BadCredentialsException) {
            message = "아이디 또는 비밀번호가 올바르지 않습니다.";
        } else {
            message = "로그인에 실패했습니다.";
        }

        String jsonResponse = String.format("""
            {
                "code": 401,
                "message": "%s"
            }
            """, message);

        response.getWriter().write(jsonResponse);
    }
}
