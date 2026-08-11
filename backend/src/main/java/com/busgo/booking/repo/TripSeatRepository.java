package com.busgo.booking.repo;

import com.busgo.booking.model.TripSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

public interface TripSeatRepository extends JpaRepository<TripSeat, UUID> {
    Optional<TripSeat> findByTripInstanceIdAndSeatCode(UUID instanceId, String seatCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TripSeat t WHERE t.tripInstanceId = :instanceId AND t.seatCode = :seatCode")
    Optional<TripSeat> lockByInstanceIdAndSeatCode(@Param("instanceId") UUID instanceId, @Param("seatCode") String seatCode);
}
