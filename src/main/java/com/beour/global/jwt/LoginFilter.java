package com.beour.global.jwt;

import com.beour.user.dto.CustomUserDetails;
import com.beour.user.dto.LoginDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

  private final AuthenticationManager authenticationManager;
  private final JWTUtil jwtUtil;

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request,
      HttpServletResponse response) {

    try {
      ObjectMapper objectMapper = new ObjectMapper();
      LoginDto loginDto = objectMapper.readValue(request.getInputStream(), LoginDto.class);

      UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
          loginDto.getLoginId(), loginDto.getPassword(), null);

      return authenticationManager.authenticate(authToken);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain, Authentication authentication)
      throws IOException {
    CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

    String loginId = customUserDetails.getUsername();

    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
    Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
    GrantedAuthority auth = iterator.next();
    String role = auth.getAuthority();

    String token = jwtUtil.createJwt(loginId, role, 60 * 60 * 10L);

    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response.setHeader("Authorization", "Bearer " + token);

    String jsonResponse = String.format("""
        {
            "code": 200,
            "message": "로그인 성공",
            "loginId": "%s",
            "role": "%s",
            "token": "Bearer %s"
        }
        """, loginId, role, token);

    response.getWriter().write(jsonResponse);
  }

  @Override
  protected void unsuccessfulAuthentication(HttpServletRequest request,
      HttpServletResponse response, AuthenticationException failed)
      throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    String message = switch (failed.getClass().getSimpleName()) {
      case "BadCredentialsException" -> "아이디 또는 비밀번호가 잘못되었습니다.";
      case "UsernameNotFoundException" -> "존재하지 않는 사용자입니다.";
      default -> "로그인에 실패했습니다.";
    };

    String jsonResponse = String.format("""
        {
            "code": 401,
            "message": "%s"
        }
        """, message);

    response.getWriter().write(jsonResponse);
  }
}
