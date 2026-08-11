package com.busgo.booking.repo;

import com.busgo.booking.model.BookingPassenger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BookingPassengerRepository extends JpaRepository<BookingPassenger, UUID> {
}
