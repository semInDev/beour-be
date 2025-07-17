package com.beour.global.jwt;

import com.beour.global.exception.error.ErrorCode;
import com.beour.global.exception.error.errorcode.UserErrorCode;
import com.beour.user.dto.CustomUserDetails;
import com.beour.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        //access토큰 없을 시 다음 필터로 넘김
        String accessToken = extractAccessToken(authorization);
        if(accessToken == null || accessToken.isBlank()){
            filterChain.doFilter(request, response);
            return;
        }

        //토큰 만료 여부 확인, 토큰 만료시 다음 필터로 넘기지 않고 만료 여부 프론트로 전달
        try{
            jwtUtil.isExpired(accessToken);
        } catch (ExpiredJwtException ex){
            writeErrorResponse(response, UserErrorCode.ACCESS_TOKEN_EXPIRED);
            return;
        }

        //token이 access token인지 확인, 아니면 프론트한테 알려줌
        if(!"access".equals(jwtUtil.getCategory(accessToken))){
            writeErrorResponse(response, UserErrorCode.NOT_ACCESS_TOKEN);
            return;
        }

        String loginId = jwtUtil.getLoginId(accessToken);
        String role = jwtUtil.getRole(accessToken);

        User user = User.fromJwt(loginId, role);
        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null,
            customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }

    private static String extractAccessToken(String authorization) {
        String[] parts = authorization.split(" ");
        return (parts.length == 2) ? parts[1] : null;
    }

    //프론트한테 가독성 좋게 에러 코드와 메세지 보내는 response body 만드는 함수
    private void writeErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getCode());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("code", errorCode.getCode());
        errorBody.put("codeName", errorCode);
        errorBody.put("message", errorCode.getMessage());

        objectMapper.writeValue(response.getWriter(), errorBody);
    }
}