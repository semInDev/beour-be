package com.beour.space.domain.repository;

import com.beour.space.domain.entity.Space;
import java.util.List;
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
}
