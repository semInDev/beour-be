package com.beour.global.jwt;

import com.beour.user.dto.CustomUserDetails;
import com.beour.user.entity.User;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
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

//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
//        FilterChain filterChain) throws ServletException, IOException {
//
//        String authorization = request.getHeader("Authorization");
//
//        //access토큰 없을 시 다음 필터로 넘김
//        if(accessToken == null){
//            filterChain.doFilter(request, response);
//            return;
//        }
//        if (authorization == null || !authorization.startsWith("Bearer ")) {
//
//            log.info("token null");
//            filterChain.doFilter(request, response);
//
//            return;
//        }
//
//        String token = authorization.split(" ")[1];
//
//        if (jwtUtil.isExpired(token)) {
//            log.info("token expired");
//            filterChain.doFilter(request, response);
//
//            return;
//        }
//
//        String loginId = jwtUtil.getLoginId(token);
//        String role = jwtUtil.getRole(token);
//
//        User user = User.fromJwt(loginId, "temppassword", role);
//        CustomUserDetails customUserDetails = new CustomUserDetails(user);
//
//        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null,
//            customUserDetails.getAuthorities());
//        SecurityContextHolder.getContext().setAuthentication(authToken);
//
//        filterChain.doFilter(request, response);
//    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        String accessToken = request.getHeader("access");

        //access토큰 없을 시 다음 필터로 넘김
        if(accessToken == null){
            filterChain.doFilter(request, response);
            return;
        }

        //토큰 만료 여부 확인, 토큰 만료시 다음 필터로 넘기지 않음
        try{
            jwtUtil.isExpired(accessToken);
        } catch (ExpiredJwtException ex){
            PrintWriter writer = response.getWriter();
            //responsebody 제대로 작성
            writer.print("access token expired");

            //accesstoken 만료시 응답 코드(생각해보기 뭐할지)
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        //token이 access token인지 확인
        String category = jwtUtil.getCategory(accessToken);
        if(!category.equals("access")){
            PrintWriter writer = response.getWriter();
            writer.print("invalid access token");

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
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
}