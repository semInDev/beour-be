package com.beour.reservation.commons.repository;

import com.beour.reservation.commons.entity.Reservation;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findBySpaceIdAndDate(Long spaceId, LocalDate date);

}
