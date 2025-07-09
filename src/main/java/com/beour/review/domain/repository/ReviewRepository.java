package com.beour.review.domain.repository;

import com.beour.review.domain.entity.Review;
import com.beour.space.domain.entity.Space;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("""
    SELECT DISTINCT r FROM Review r
    LEFT JOIN FETCH r.comment
    LEFT JOIN FETCH r.images
    WHERE r.guest.id = :guestId AND r.deletedAt IS NULL
    """)
    List<Review> findAllWithCommentAndImagesByGuestId(@Param("guestId") Long guestId);

    Optional<Review> findByGuestIdAndSpaceIdAndReservedDateAndDeletedAtIsNull(Long guestId, Long spaceId, LocalDate reservedDate);

    long countBySpaceIdAndDeletedAtIsNull(Long spaceId);
    List<Review> findTop5ByDeletedAtIsNullOrderByCreatedAtDesc();

}
