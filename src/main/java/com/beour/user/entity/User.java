package com.beour.user.entity;

import com.beour.user.dto.SignupDto;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;
    private String nickname;
    private String email;
    private String loginId;
    private String password;
    private String phone;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    @Builder
    public User(String name, String nickname, String email, String loginId, String password, String phone) {
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.loginId = loginId;
        this.password = password;
        this.phone = phone;
    }

    public static User createFrom(SignupDto signUpDto){
        return User.builder()
                .name(signUpDto.getName())
                .nickname(signUpDto.getNickname())
                .email(signUpDto.getEmail())
                .loginId(signUpDto.getLoginId())
                .password(signUpDto.getPassword())
                .phone(signUpDto.getPhone())
                .build();
    }

}
