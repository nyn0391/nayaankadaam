package com.busgo.trip;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "trips")
public class Trip {
    @Id
    private UUID id;
    private UUID routeId;
    private UUID busId;
    private UUID operatorId;
    private OffsetDateTime departureAt;
    private OffsetDateTime arrivalAt;
    private Double basePrice;

    public Trip() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getRouteId() { return routeId; }
    public void setRouteId(UUID routeId) { this.routeId = routeId; }
    public UUID getBusId() { return busId; }
    public void setBusId(UUID busId) { this.busId = busId; }
    public UUID getOperatorId() { return operatorId; }
    public void setOperatorId(UUID operatorId) { this.operatorId = operatorId; }
    public OffsetDateTime getDepartureAt() { return departureAt; }
    public void setDepartureAt(OffsetDateTime departureAt) { this.departureAt = departureAt; }
    public OffsetDateTime getArrivalAt() { return arrivalAt; }
    public void setArrivalAt(OffsetDateTime arrivalAt) { this.arrivalAt = arrivalAt; }
    public Double getBasePrice() { return basePrice; }
    public void setBasePrice(Double basePrice) { this.basePrice = basePrice; }
}
