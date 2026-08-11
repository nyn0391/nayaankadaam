package com.busgo.trip;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "trip_instances")
public class TripInstance {
    @Id
    private UUID id;

    private UUID templateId;
    private OffsetDateTime departureAt;
    private OffsetDateTime arrivalAt;
    private String status; // SCHEDULED | CANCELLED
    private Double instancePrice;
    private String timezone;
    private UUID createdBy;
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public TripInstance() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTemplateId() { return templateId; }
    public void setTemplateId(UUID templateId) { this.templateId = templateId; }
    public OffsetDateTime getDepartureAt() { return departureAt; }
    public void setDepartureAt(OffsetDateTime departureAt) { this.departureAt = departureAt; }
    public OffsetDateTime getArrivalAt() { return arrivalAt; }
    public void setArrivalAt(OffsetDateTime arrivalAt) { this.arrivalAt = arrivalAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getInstancePrice() { return instancePrice; }
    public void setInstancePrice(Double instancePrice) { this.instancePrice = instancePrice; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
