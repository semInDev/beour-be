package com.beour.review.domain.repository;

import com.beour.review.domain.entity.ReviewComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewCommentRepository extends JpaRepository<ReviewComment, Long> {

    @Query("""
    SELECT rc FROM ReviewComment rc
    LEFT JOIN FETCH rc.review r
    LEFT JOIN FETCH r.guest
    LEFT JOIN FETCH r.space
    LEFT JOIN FETCH r.images
    WHERE rc.user.id = :hostId 
    AND rc.deletedAt IS NULL
    ORDER BY rc.createdAt DESC
    """)
    Page<ReviewComment> findWrittenCommentsByHostId(@Param("hostId") Long hostId, Pageable pageable);

}
