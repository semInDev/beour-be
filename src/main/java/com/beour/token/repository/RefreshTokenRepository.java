package com.beour.token.repository;

import com.beour.token.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Boolean existsByRefresh(String refresh);
    Boolean existsByLoginId(String loginId);

    @Transactional
    void deleteByRefresh(String refresh);
}
