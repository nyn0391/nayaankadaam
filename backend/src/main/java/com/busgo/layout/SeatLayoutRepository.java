package com.busgo.layout;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SeatLayoutRepository extends JpaRepository<SeatLayout, UUID> {
}
