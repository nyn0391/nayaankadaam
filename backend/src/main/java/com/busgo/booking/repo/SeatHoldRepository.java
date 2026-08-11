package com.busgo.booking.repo;

import com.busgo.booking.SeatHold;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface SeatHoldRepository extends JpaRepository<SeatHold, UUID> {
    List<SeatHold> findByHoldToken(String holdToken);
    List<SeatHold> findByTripInstanceIdAndSeatCodeAndExpiresAtAfter(UUID tripInstanceId, String seatCode, OffsetDateTime now);
    List<SeatHold> findByExpiresAtBefore(OffsetDateTime now);
    void deleteByHoldToken(String holdToken);
}
