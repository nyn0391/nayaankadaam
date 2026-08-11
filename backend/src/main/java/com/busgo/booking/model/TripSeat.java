package com.busgo.booking.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "trip_seats")
public class TripSeat {
    @Id
    private UUID id;

    private UUID tripId;
    private String seatCode;
    private Boolean isBooked = false;
    private BigDecimal price = BigDecimal.ZERO;

    public TripSeat() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTripId() { return tripId; }
    public void setTripId(UUID tripId) { this.tripId = tripId; }
    public String getSeatCode() { return seatCode; }
    public void setSeatCode(String seatCode) { this.seatCode = seatCode; }
    public Boolean getIsBooked() { return isBooked; }
    public void setIsBooked(Boolean isBooked) { this.isBooked = isBooked; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
