package com.busgo.bus;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "buses")
public class Bus {
    @Id
    private UUID id;
    private UUID operatorId;
    private String model;
    private String registrationNumber;
    private UUID seatLayoutId;
    private Integer totalSeats;
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public Bus() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getOperatorId() { return operatorId; }
    public void setOperatorId(UUID operatorId) { this.operatorId = operatorId; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public UUID getSeatLayoutId() { return seatLayoutId; }
    public void setSeatLayoutId(UUID seatLayoutId) { this.seatLayoutId = seatLayoutId; }
    public Integer getTotalSeats() { return totalSeats; }
    public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
