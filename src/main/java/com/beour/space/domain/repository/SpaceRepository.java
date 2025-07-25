package com.beour.space.domain.repository;

import com.beour.space.domain.entity.Space;
import com.beour.space.domain.enums.SpaceCategory;
import com.beour.space.domain.enums.UseCategory;
import java.time.LocalDate;
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
        SELECT *
        FROM space s
        WHERE s.deleted_at IS NULL
          AND ST_Distance_Sphere(POINT(s.longitude, s.latitude), POINT(:longitude, :latitude)) <= :radius
        ORDER BY ST_Distance_Sphere(POINT(s.longitude, s.latitude), POINT(:longitude, :latitude)) ASC
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM space s
        WHERE s.deleted_at IS NULL
          AND ST_Distance_Sphere(POINT(s.longitude, s.latitude), POINT(:longitude, :latitude)) <= :radius
        """,
            nativeQuery = true)
    Page<Space> findAllWithinDistanceWithPaging(@Param("latitude") double latitude,
                                                @Param("longitude") double longitude,
                                                @Param("radius") double radiusInMeters,
                                                Pageable pageable);

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

    Page<Space> findByUseCategory(UseCategory useCategory, Pageable pageable);

    Page<Space> findByHostAndDeletedAtIsNull(User host, Pageable pageable);

    List<Space> findTop5ByDeletedAtIsNullOrderByCreatedAtDesc();


    Optional<Space> findByIdAndDeletedAtIsNull(Long id);

    List<Space> findByHostAndDeletedAtIsNull(User host);

    @Query(value = """
        SELECT DISTINCT s.*
        FROM space s
        LEFT JOIN description d ON s.id = d.space_id
        LEFT JOIN tag t ON s.id = t.space_id
        WHERE (
            d.description LIKE %:keyword%
            OR d.price_guide LIKE %:keyword%
            OR d.facility_notice LIKE %:keyword%
            OR d.notice LIKE %:keyword%
            OR d.location_description LIKE %:keyword%
            OR d.refund_policy LIKE %:keyword%
            OR d.website_url LIKE %:keyword%
            OR t.contents LIKE %:keyword%
            OR s.address LIKE %:keyword%
        )
        AND (:minPrice IS NULL OR s.price_per_hour >= :minPrice)
        AND (:maxPrice IS NULL OR s.price_per_hour <= :maxPrice)
        AND (:address IS NULL OR s.address LIKE %:address%)
        AND (:minCapacity IS NULL OR s.max_capacity >= :minCapacity)
        AND (:spaceCategories IS NULL OR s.space_category IN (:spaceCategories))
        AND (:useCategories IS NULL OR s.use_category IN (:useCategories))
        AND (
            :date IS NULL OR EXISTS (
                SELECT 1 FROM available_time at
                WHERE at.space_id = s.id AND at.date = :date AND at.deleted_at IS NULL
            )
        )
        """,
        countQuery = """
            SELECT COUNT(DISTINCT s.id)
            FROM space s
            LEFT JOIN description d ON s.id = d.space_id
            LEFT JOIN tag t ON s.id = t.space_id
            WHERE (
                d.description LIKE %:keyword%
                OR d.price_guide LIKE %:keyword%
                OR d.facility_notice LIKE %:keyword%
                OR d.notice LIKE %:keyword%
                OR d.location_description LIKE %:keyword%
                OR d.refund_policy LIKE %:keyword%
                OR d.website_url LIKE %:keyword%
                OR t.contents LIKE %:keyword%
                OR s.address LIKE %:keyword%
            )
            AND (:minPrice IS NULL OR s.price_per_hour >= :minPrice)
            AND (:maxPrice IS NULL OR s.price_per_hour <= :maxPrice)
            AND (:address IS NULL OR s.address LIKE %:address%)
            AND (:minCapacity IS NULL OR s.max_capacity >= :minCapacity)
            AND (:spaceCategories IS NULL OR s.space_category IN (:spaceCategories))
            AND (:useCategories IS NULL OR s.use_category IN (:useCategories))
            AND (
                :date IS NULL OR EXISTS (
                    SELECT 1 FROM available_time at
                    WHERE at.space_id = s.id AND at.date = :date AND at.deleted_at IS NULL
                )
            )
            """,
        nativeQuery = true)
    Page<Space> searchWithFiltering(
        @Param("keyword") String keyword,
        @Param("minPrice") Integer minPrice,
        @Param("maxPrice") Integer maxPrice,
        @Param("address") String address,
        @Param("minCapacity") Integer minCapacity,
        @Param("spaceCategories") List<SpaceCategory> spaceCategories,
        @Param("useCategories") List<UseCategory> useCategories,
        @Param("date") LocalDate date,
        Pageable pageable
    );
}
