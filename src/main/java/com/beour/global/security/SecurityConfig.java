package com.beour.global.security;

import com.beour.global.jwt.CustomLogoutFilter;
import com.beour.global.jwt.JWTFilter;
import com.beour.global.jwt.JWTUtil;
import com.beour.global.jwt.LoginFilter;
import com.beour.token.repository.RefreshTokenRepository;
import com.beour.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        AuthenticationManager authenticationManager = authenticationManager();

        LoginFilter loginFilter = new LoginFilter(authenticationManager, userRepository, jwtUtil,
            refreshTokenRepository);
        loginFilter.setFilterProcessesUrl("/api/login");

        http.cors((cors) -> cors.configurationSource(corsConfigurationSource));

        http
            .authorizeHttpRequests((auth) -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                // all - 로그인, 회원가입 등
                .requestMatchers("/api/signup", "/api/signup/check-duplicate/login-id",
                    "/api/signup/check-duplicate/nickname", "/api/login",
                    "/api/users/find/login-id", "/api/users/reset/password")
                .permitAll()

                // all - 공간 검색 및 이용가능 시간
                .requestMatchers("/api/spaces/*/available-times/date", "/api/reviews/new",
                    "/api/spaces/nearby", "/api/spaces/keyword", "/api/spaces/filter",
                    "/api/spaces/spacecategory", "/api/spaces/usecategory", "/api/spaces/new")
                .permitAll()

                //host - 공간 예약
                .requestMatchers("/api/reservations/condition", "/api/reservations/*/accept",
                    "/api/reservations/*/reject", "/api/users/me/spaces-name", "/api/reservations",
                    "/api/spaces/reservations").hasRole("HOST")

                //host - 공간
                .requestMatchers("/api/spaces", "/api/spaces/*/simple", "/api/spaces/*",
                    "/api/spaces/*/basic", "/api/spaces/*/description", "/api/spaces/*/tags",
                    "/api/spaces/*/images", "/api/spaces/*/available-times").hasRole("HOST")

                // host - 댓글
                .requestMatchers("/api/users/me/commentable-reviews",
                    "/api/users/me/review-comments", "/api/users/me/review-comments/*")
                .hasRole("HOST")

                // guest - 예약
                .requestMatchers("/api/spaces/*/reservations", "/api/reservations/current",
                    "/api/reservations/past", "/api/reservations/*").hasRole("GUEST")

                // guest - 리뷰
                .requestMatchers("/api/users/me/reviewable-reservations", "/api/users/me/reviews",
                    "/api/reviews/reservations/*", "/api/users/me/reviews/*").hasRole("GUEST")

                // guest - 찜
                .requestMatchers("/api/likes", "/api/spaces/*/likes").hasRole("GUEST")

                //admin
                .requestMatchers("/api/banners", "/admin/banner/list").hasRole("ADMIN")

                //guest, host, admin
                .requestMatchers("/api/token/reissue", "api/users", "api/users/me",
                    "api/users/me/detail", "api/users/me/password", "/api/logout")
                .hasAnyRole("HOST", "GUEST", "ADMIN")

                .anyRequest().authenticated()
            );

        http.exceptionHandling(exception -> exception
            .authenticationEntryPoint((request, response, authException) -> {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            })
            .accessDeniedHandler((request, response, accessDeniedException) -> {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
            })
        );

        http
            .addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class)
            .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(new CustomLogoutFilter(jwtUtil, refreshTokenRepository),
                LogoutFilter.class);

        http
            .formLogin((auth) -> auth.disable())
            .csrf((auth) -> auth.disable())
            .httpBasic((auth) -> auth.disable())
            .sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}