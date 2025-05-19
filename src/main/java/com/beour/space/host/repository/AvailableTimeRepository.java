package com.beour.space.host.repository;

import com.beour.space.host.entity.AvailableTime;
import com.beour.space.host.entity.Space;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvailableTimeRepository extends JpaRepository<AvailableTime, Long> {
    void deleteBySpace(Space space);
}

