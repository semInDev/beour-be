package com.beour.space.domain.repository;

import com.beour.space.domain.entity.Space;
import com.beour.space.domain.enums.SpaceCategory;
import com.beour.space.domain.enums.UseCategory;
import java.util.List;
import java.util.Optional;
import com.beour.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface SpaceRepository extends JpaRepository<Space, Long> {

    @Query(value = """
        SELECT *
        FROM space s
        WHERE s.deleted_at IS NULL
          AND ST_Distance_Sphere(POINT(s.longitude, s.latitude), POINT(:longitude, :latitude)) <= :radius
        ORDER BY ST_Distance_Sphere(POINT(s.longitude, s.latitude), POINT(:longitude, :latitude)) ASC
        """, nativeQuery = true)
    List<Space> findAllWithinDistance(@Param("latitude") double latitude,
        @Param("longitude") double longitude,
        @Param("radius") double radiusInMeters);


    @Query(value = """
    SELECT DISTINCT s.*
    FROM space s
    LEFT JOIN description d ON s.id = d.space_id
    LEFT JOIN tag t ON s.id = t.space_id
    WHERE d.description LIKE %:keyword%
       OR d.price_guide LIKE %:keyword%
       OR d.facility_notice LIKE %:keyword%
       OR d.notice LIKE %:keyword%
       OR d.location_description LIKE %:keyword%
       OR d.refund_policy LIKE %:keyword%
       OR d.website_url LIKE %:keyword%
       OR t.contents LIKE %:keyword%
       OR s.address LIKE %:keyword%
    """, nativeQuery = true)
    List<Space> searchByKeyword(@Param("keyword") String keyword);

    @Query(value = """
    SELECT DISTINCT s.*
    FROM space s
    LEFT JOIN description d ON s.id = d.space_id
    LEFT JOIN tag t ON s.id = t.space_id
    WHERE d.description LIKE %:keyword%
       OR d.price_guide LIKE %:keyword%
       OR d.facility_notice LIKE %:keyword%
       OR d.notice LIKE %:keyword%
       OR d.location_description LIKE %:keyword%
       OR d.refund_policy LIKE %:keyword%
       OR d.website_url LIKE %:keyword%
       OR t.contents LIKE %:keyword%
       OR s.address LIKE %:keyword%
    """, nativeQuery = true)
    Page<Space> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    Page<Space> findBySpaceCategory(SpaceCategory spaceCategory, Pageable pageable);
    List<Space> findByUseCategory(UseCategory useCategory);
    List<Space> findTop5ByDeletedAtIsNullOrderByCreatedAtDesc();


    Optional<Space> findByIdAndDeletedAtIsNull(Long id);
    List<Space> findByHostAndDeletedAtIsNull(User host);
}
