package com.busgo.trip;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "trip_templates")
public class TripTemplate {
    @Id
    private UUID id;
    private String name;
    private UUID routeId;
    private UUID busId;
    private Double basePrice;
    private String notes;
    private UUID createdBy;
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public TripTemplate() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public UUID getRouteId() { return routeId; }
    public void setRouteId(UUID routeId) { this.routeId = routeId; }
    public UUID getBusId() { return busId; }
    public void setBusId(UUID busId) { this.busId = busId; }
    public Double getBasePrice() { return basePrice; }
    public void setBasePrice(Double basePrice) { this.basePrice = basePrice; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
