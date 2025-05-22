package com.beour.reservation.commons.repository;

import com.beour.reservation.commons.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

}
