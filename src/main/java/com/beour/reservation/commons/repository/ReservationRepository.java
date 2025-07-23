package com.beour.reservation.commons.repository;

import com.beour.reservation.commons.entity.Reservation;
import com.beour.reservation.commons.enums.ReservationStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findBySpaceIdAndDateAndDeletedAtIsNull(Long spaceId, LocalDate date);

    List<Reservation> findBySpaceIdAndDateAndStatusNot(Long spaceId, LocalDate date,
        ReservationStatus status);

    @Query("SELECT r FROM Reservation r JOIN FETCH r.space " +
        "WHERE r.guest.id = :guestId AND " +
        "(r.date > :today OR (r.date = :today AND r.endTime > :now))")
    Page<Reservation> findUpcomingReservationsByGuest(
        @Param("guestId") Long guestId,
        @Param("today") LocalDate today,
        @Param("now") LocalTime now,
        Pageable pageable
    );

    @Query("SELECT r FROM Reservation r " +
        "WHERE r.guest.id = :guestId AND " +
        "(r.date < :today OR (r.date = :today AND r.endTime <= :now))")
    Page<Reservation> findPastReservationsByGuest(
        @Param("guestId") Long guestId,
        @Param("today") LocalDate today,
        @Param("now") LocalTime now,
        Pageable pageable
    );

    @Query("SELECT r FROM Reservation r JOIN FETCH r.space WHERE r.guest.id = :guestId AND r.status = 'COMPLETED' AND r.deletedAt IS NULL")
    List<Reservation> findCompletedReservationsWithSpaceByGuestId(@Param("guestId") Long guestId);

    List<Reservation> findByHostIdAndDateAndDeletedAtIsNull(Long hostId, LocalDate date);

    List<Reservation> findByHostIdAndDateAndSpaceIdAndDeletedAtIsNull(Long hostId, LocalDate date,
        Long spaceId);

    List<Reservation> findByHostIdAndDateAndStatusAndDeletedAtIsNull(Long hostId, LocalDate date,
        ReservationStatus status);

    List<Reservation> findByHostIdAndDateAndSpaceIdAndStatusAndDeletedAtIsNull(Long hostId,
        LocalDate date, Long spaceId, ReservationStatus status);

    List<Reservation> findByHostIdAndStatusInAndDeletedAtIsNull(Long hostId,
        List<ReservationStatus> statuses);
    List<Reservation> findByGuestIdAndStatusInAndDeletedAtIsNull(Long guestId,
        List<ReservationStatus> statuses);
}
