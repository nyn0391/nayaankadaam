package com.busgo.booking;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "seat_holds")
public class SeatHold {
    @Id
    private UUID id;

    private UUID tripInstanceId;
    private String seatCode;
    private UUID userId;
    private String holdToken;
    private OffsetDateTime createdAt = OffsetDateTime.now();
    private OffsetDateTime expiresAt;
    @Column(columnDefinition = "jsonb")
    private String metadata;

    public SeatHold() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTripInstanceId() { return tripInstanceId; }
    public void setTripInstanceId(UUID tripInstanceId) { this.tripInstanceId = tripInstanceId; }
    public String getSeatCode() { return seatCode; }
    public void setSeatCode(String seatCode) { this.seatCode = seatCode; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getHoldToken() { return holdToken; }
    public void setHoldToken(String holdToken) { this.holdToken = holdToken; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(OffsetDateTime expiresAt) { this.expiresAt = expiresAt; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
}
