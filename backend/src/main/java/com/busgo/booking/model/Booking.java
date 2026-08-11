package com.busgo.booking.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    private UUID id;

    private UUID userId;
    private UUID tripId; // legacy/template-level
    private UUID tripInstanceId; // new: instance-level booking
    private String status;
    private BigDecimal amount;
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public Booking() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getTripId() { return tripId; }
    public void setTripId(UUID tripId) { this.tripId = tripId; }
    public UUID getTripInstanceId() { return tripInstanceId; }
    public void setTripInstanceId(UUID tripInstanceId) { this.tripInstanceId = tripInstanceId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
