package com.busgo.trip;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface TripInstanceRepository extends JpaRepository<TripInstance, UUID> {
    java.util.List<TripInstance> findByTemplateId(UUID templateId);
    boolean existsByTemplateIdAndDepartureAt(UUID templateId, OffsetDateTime departureAt);
}
