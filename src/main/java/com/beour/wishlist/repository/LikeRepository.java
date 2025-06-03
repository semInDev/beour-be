package com.beour.wishlist.repository;

import com.beour.wishlist.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> {
    boolean existsByUserIdAndSpaceId(Long userId, Long spaceId);

}

