package com.busgo.trip;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TripInstanceRepository extends JpaRepository<TripInstance, UUID> {
    List<TripInstance> findByTemplateId(UUID templateId);
}
