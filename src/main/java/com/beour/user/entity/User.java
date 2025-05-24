package com.beour.user.entity;

import com.beour.global.entity.BaseTimeEntity;
import com.beour.user.dto.SignupDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = 10)
    private String name;

    @Column(nullable = false, unique = true, length = 10)
    private String nickname;

    @Column(nullable = false, length = 50)
    private String email;

    @Column(nullable = false, unique = true, length = 15)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String role;

    @Builder
    public User(String name, String nickname, String email, String loginId, String password,
        String phone, String role) {
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.loginId = loginId;
        this.password = password;
        this.phone = phone;
        this.role = role;
    }

    public static User createFrom(SignupDto signUpDto) {
        return User.builder()
            .name(signUpDto.getName())
            .nickname(signUpDto.getNickname())
            .email(signUpDto.getEmail())
            .loginId(signUpDto.getLoginId())
            .password(signUpDto.getPassword())
            .phone(signUpDto.getPhone())
            .role(signUpDto.getRole())
            .build();
    }

    public static User fromJwt(String loginId, String password, String role) {
        return User.builder()
            .loginId(loginId)
            .password(password)
            .role(role)
            .build();
    }

}
