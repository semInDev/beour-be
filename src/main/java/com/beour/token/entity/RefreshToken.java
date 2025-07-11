package com.beour.token.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String loginId;
    private String refresh;
    private String expiration;

    @Builder
    public RefreshToken(String loginId, String refresh, String expiration) {
        this.loginId = loginId;
        this.refresh = refresh;
        this.expiration = expiration;
    }
}
