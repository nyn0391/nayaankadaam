package com.busgo.booking.api;

import java.time.OffsetDateTime;
import java.util.UUID;

public class SeatEvent {
    public enum Type { HELD, RELEASED, BOOKED }

    private UUID instanceId;
    private String seatCode;
    private Type eventType;
    private String holdToken;
    private OffsetDateTime expiresAt;
    private UUID userId;
    private UUID bookingId;

    public SeatEvent() {}

    public UUID getInstanceId() { return instanceId; }
    public void setInstanceId(UUID instanceId) { this.instanceId = instanceId; }
    public String getSeatCode() { return seatCode; }
    public void setSeatCode(String seatCode) { this.seatCode = seatCode; }
    public Type getEventType() { return eventType; }
    public void setEventType(Type eventType) { this.eventType = eventType; }
    public String getHoldToken() { return holdToken; }
    public void setHoldToken(String holdToken) { this.holdToken = holdToken; }
    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(OffsetDateTime expiresAt) { this.expiresAt = expiresAt; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }
}
