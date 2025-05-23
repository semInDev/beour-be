package com.beour.space.domain.repository;

import com.beour.space.domain.entity.Space;
import com.beour.space.domain.entity.SpaceImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpaceImageRepository extends JpaRepository<SpaceImage, Long> {
    void deleteBySpace(Space space);
}
