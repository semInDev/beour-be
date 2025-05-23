package com.beour.space.host.repository;

import com.beour.space.host.entity.AvailableTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvailableTimeRepository extends JpaRepository<AvailableTime, Long> {
    Optional<AvailableTime> findBySpaceIdAndDate(Long spaceId, LocalDate date);
}

