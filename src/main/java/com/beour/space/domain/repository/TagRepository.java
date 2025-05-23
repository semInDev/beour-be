package com.beour.space.domain.repository;

import com.beour.space.domain.entity.Space;
import com.beour.space.domain.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
    void deleteBySpace(Space space);
}
