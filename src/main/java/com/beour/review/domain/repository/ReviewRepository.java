package com.beour.review.domain.repository;

import com.beour.review.domain.entity.Review;
import com.beour.space.domain.entity.Space;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByGuestIdAndSpaceIdAndReservedDateAndDeletedAtIsNull(Long guestId, Long spaceId, LocalDate reservedDate);

    long countBySpaceIdAndDeletedAtIsNull(Long spaceId);
    List<Review> findTop5ByDeletedAtIsNullOrderByCreatedAtDesc();
}
