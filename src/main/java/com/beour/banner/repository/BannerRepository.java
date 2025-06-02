package com.beour.banner.repository;

import com.beour.banner.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BannerRepository extends JpaRepository<Banner, Long> {

}
