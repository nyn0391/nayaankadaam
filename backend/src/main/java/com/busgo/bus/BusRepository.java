package com.busgo.bus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BusRepository extends JpaRepository<Bus, UUID> {
    Optional<Bus> findByRegistrationNumber(String registrationNumber);
}
