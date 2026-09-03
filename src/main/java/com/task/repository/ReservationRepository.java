package com.task.repository;

import com.task.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    @Query("SELECT r FROM Reservation r JOIN FETCH r.user JOIN FETCH r.resource WHERE r.id = :id")
    Optional<Reservation> findByIdWithDetails(@Param("id") Long id);
}
