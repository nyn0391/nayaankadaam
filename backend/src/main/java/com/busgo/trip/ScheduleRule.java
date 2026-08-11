package com.busgo.trip;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "schedule_rules")
public class ScheduleRule {
    @Id
    private UUID id;

    private UUID templateId;
    private String ruleType; // ONE_OFF | DAILY | WEEKLY
    private java.sql.Date startDate;
    private java.sql.Date endDate;
    @Column(columnDefinition = "jsonb")
    private String weekdays;
    private String timeOfDay;
    private String timezone;
    private UUID createdBy;
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public ScheduleRule() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTemplateId() { return templateId; }
    public void setTemplateId(UUID templateId) { this.templateId = templateId; }
    public String getRuleType() { return ruleType; }
    public void setRuleType(String ruleType) { this.ruleType = ruleType; }
    public java.sql.Date getStartDate() { return startDate; }
    public void setStartDate(java.sql.Date startDate) { this.startDate = startDate; }
    public java.sql.Date getEndDate() { return endDate; }
    public void setEndDate(java.sql.Date endDate) { this.endDate = endDate; }
    public String getWeekdays() { return weekdays; }
    public void setWeekdays(String weekdays) { this.weekdays = weekdays; }
    public String getTimeOfDay() { return timeOfDay; }
    public void setTimeOfDay(String timeOfDay) { this.timeOfDay = timeOfDay; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
