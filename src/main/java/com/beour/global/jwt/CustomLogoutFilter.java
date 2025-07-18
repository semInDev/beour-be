package com.beour.global.jwt;

import com.beour.global.exception.error.errorcode.UserErrorCode;
import com.beour.global.exception.exceptionType.TokenExpiredException;
import com.beour.global.exception.exceptionType.TokenNotFoundException;
import com.beour.token.repository.RefreshTokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.GenericFilterBean;

@RequiredArgsConstructor
public class CustomLogoutFilter extends GenericFilterBean {

    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        //api 요청 올바른지 체크하는 로직
        if (!request.getRequestURI().equals("/api/logout") || !request.getMethod().equals("POST")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String refresh = extractRefreshToken(request);

            if (jwtUtil.isExpired(refresh)) {
                throw new TokenExpiredException(UserErrorCode.REFRESH_TOKEN_EXPIRED);
            }

            if (!"refresh".equals(jwtUtil.getCategory(refresh))) {
                throw new TokenNotFoundException(UserErrorCode.REFRESH_TOKEN_NOT_FOUND);
            }

            //DB에 저장되어 있는지 확인
            Boolean isExist = refreshTokenRepository.existsByRefresh(refresh);
            if (!refreshTokenRepository.existsByRefresh(refresh)) {
                throw new TokenNotFoundException(UserErrorCode.REFRESH_TOKEN_NOT_FOUND);
            }

            //로그아웃 진행
            //Refresh 토큰 DB에서 제거
            refreshTokenRepository.deleteByRefresh(refresh);
            clearRefreshCookie(response);

            response.setStatus(HttpServletResponse.SC_OK);

        } catch (TokenNotFoundException | TokenExpiredException e) {
            int code = e instanceof TokenNotFoundException ? 404 : 401;
            Enum codeName = e instanceof TokenNotFoundException ? UserErrorCode.REFRESH_TOKEN_NOT_FOUND : UserErrorCode.REFRESH_TOKEN_EXPIRED;

            writeJsonErrorResponse(response, code, codeName, e.getMessage());
        }

    }

    private static void clearRefreshCookie(HttpServletResponse response) {
        //Refresh 토큰 Cookie 값 0
        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new TokenNotFoundException(UserErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        for (Cookie cookie : cookies) {
            if ("refresh".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        throw new TokenNotFoundException(UserErrorCode.REFRESH_TOKEN_NOT_FOUND);
    }

    private void writeJsonErrorResponse(HttpServletResponse response, int code, Enum codeName, String message) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorBody = Map.of("code", code, "codeName", codeName, "message", message);

        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(errorBody));
    }
}
