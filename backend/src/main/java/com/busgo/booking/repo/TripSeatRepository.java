package com.busgo.booking.repo;

import com.busgo.booking.model.TripSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TripSeatRepository extends JpaRepository<TripSeat, UUID> {
    Optional<TripSeat> findByTripIdAndSeatCode(UUID tripId, String seatCode);
}
