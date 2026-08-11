package com.busgo.trip;

import com.busgo.booking.repo.TripSeatRepository;
import com.busgo.bus.BusRepository;
import com.busgo.layout.SeatLayoutRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ScheduleExpansionService {

    private final ScheduleRuleRepository scheduleRuleRepository;
    private final TripTemplateRepository templateRepository;
    private final TripInstanceRepository instanceRepository;
    private final BusRepository busRepository;
    private final SeatLayoutRepository seatLayoutRepository;
    private final TripSeatRepository tripSeatRepository;

    public ScheduleExpansionService(ScheduleRuleRepository scheduleRuleRepository,
                                    TripTemplateRepository templateRepository,
                                    TripInstanceRepository instanceRepository,
                                    BusRepository busRepository,
                                    SeatLayoutRepository seatLayoutRepository,
                                    TripSeatRepository tripSeatRepository) {
        this.scheduleRuleRepository = scheduleRuleRepository;
        this.templateRepository = templateRepository;
        this.instanceRepository = instanceRepository;
        this.busRepository = busRepository;
        this.seatLayoutRepository = seatLayoutRepository;
        this.tripSeatRepository = tripSeatRepository;
    }

    @Transactional
    public List<java.util.UUID> expandRule(java.util.UUID ruleId, LocalDate startDate, LocalDate endDate) {
        var created = new ArrayList<java.util.UUID>();
        var ruleOpt = scheduleRuleRepository.findById(ruleId);
        if (ruleOpt.isEmpty()) return created;
        var rule = ruleOpt.get();
        var templateOpt = templateRepository.findById(rule.getTemplateId());
        if (templateOpt.isEmpty()) return created;
        var template = templateOpt.get();

        LocalDate effectiveStart = startDate.isAfter(rule.getStartDate().toLocalDate()) ? startDate : rule.getStartDate().toLocalDate();
        LocalDate effectiveEnd = endDate;
        if (rule.getEndDate() != null) {
            LocalDate ruleEnd = rule.getEndDate().toLocalDate();
            if (ruleEnd.isBefore(effectiveEnd)) effectiveEnd = ruleEnd;
        }

        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        ZoneId zone = (rule.getTimezone() != null) ? ZoneId.of(rule.getTimezone()) : ZoneOffset.UTC;

        Set<DayOfWeek> weekdays = new HashSet<>();
        if (rule.getWeekdays() != null && !rule.getWeekdays().isEmpty()) {
            try {
                var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                String json = rule.getWeekdays();
                var arr = mapper.readValue(json, String[].class);
                for (String w : arr) {
                    weekdays.add(DayOfWeek.valueOf(w));
                }
            } catch (Exception ignored) {}
        }

        for (LocalDate d = effectiveStart; !d.isAfter(effectiveEnd); d = d.plusDays(1)) {
            boolean matches = false;
            switch (rule.getRuleType()) {
                case "ONE_OFF":
                    matches = d.equals(rule.getStartDate().toLocalDate());
                    break;
                case "DAILY":
                    matches = true;
                    break;
                case "WEEKLY":
                    matches = weekdays.contains(d.getDayOfWeek());
                    break;
            }
            if (!matches) continue;

            // construct ZonedDateTime from date + timeOfDay + timezone
            if (rule.getTimeOfDay() == null) continue; // cannot schedule without time
            LocalTime lt = LocalTime.parse(rule.getTimeOfDay(), timeFmt);
            ZonedDateTime zdt = ZonedDateTime.of(d, lt, zone);
            OffsetDateTime odt = zdt.toOffsetDateTime();

            // idempotency: check if instance exists for template+departureAt
            boolean exists = instanceRepository.existsByTemplateIdAndDepartureAt(template.getId(), odt);
            if (exists) continue;

            // create instance
            TripInstance inst = new TripInstance();
            inst.setId(UUID.randomUUID());
            inst.setTemplateId(template.getId());
            inst.setDepartureAt(odt);
            inst.setTimezone(zone.toString());
            inst.setStatus("SCHEDULED");
            // instancePrice fallbacks to template.basePrice
            inst.setInstancePrice(template.getBasePrice());
            instanceRepository.save(inst);

            // generate seats
            if (template.getBusId() != null) {
                busRepository.findById(template.getBusId()).ifPresent(bus -> {
                    if (bus.getSeatLayoutId() != null) {
                        seatLayoutRepository.findById(bus.getSeatLayoutId()).ifPresent(layout -> {
                            try {
                                com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                                var node = om.readTree(layout.getContent());
                                var seats = node.get("seats");
                                if (seats != null && seats.isArray()) {
                                    for (var s : seats) {
                                        String seatCode = s.get("seatCode").asText();
                                        var ts = new com.busgo.booking.model.TripSeat();
                                        ts.setId(UUID.randomUUID());
                                        ts.setTripInstanceId(inst.getId());
                                        ts.setSeatCode(seatCode);
                                        ts.setIsBooked(false);
                                        if (s.has("price")) ts.setPrice(java.math.BigDecimal.valueOf(s.get("price").asDouble()));
                                        tripSeatRepository.save(ts);
                                    }
                                }
                            } catch (Exception ex) {
                                // ignore seat generation errors per-instance
                            }
                        });
                    }
                });
            }

            created.add(inst.getId());
        }

        return created;
    }

    @Transactional
    public List<java.util.UUID> expandAll(LocalDate start, LocalDate end) {
        List<java.util.UUID> created = new ArrayList<>();
        var rules = scheduleRuleRepository.findAll();
        for (var r : rules) {
            created.addAll(expandRule(r.getId(), start, end));
        }
        return created;
    }
}
