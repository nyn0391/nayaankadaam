package com.busgo.trip;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/schedules")
public class ScheduleAdminController {

    private final ScheduleRuleRepository scheduleRuleRepository;
    private final ScheduleExpansionService expansionService;

    public ScheduleAdminController(ScheduleRuleRepository scheduleRuleRepository, ScheduleExpansionService expansionService) {
        this.scheduleRuleRepository = scheduleRuleRepository;
        this.expansionService = expansionService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createRule(@RequestBody Map<String,Object> body) {
        try {
            ScheduleRule r = new ScheduleRule();
            if (body.containsKey("id")) r.setId(UUID.fromString((String) body.get("id"))); else r.setId(UUID.randomUUID());
            r.setTemplateId(UUID.fromString((String) body.get("templateId")));
            r.setRuleType((String) body.get("ruleType"));
            r.setStartDate(java.sql.Date.valueOf((String) body.get("startDate")));
            if (body.containsKey("endDate") && body.get("endDate") != null) r.setEndDate(java.sql.Date.valueOf((String) body.get("endDate")));
            if (body.containsKey("weekdays")) r.setWeekdays(body.get("weekdays").toString());
            r.setTimeOfDay((String) body.get("timeOfDay"));
            r.setTimezone((String) body.getOrDefault("timezone", "UTC"));
            scheduleRuleRepository.save(r);
            return ResponseEntity.ok(r);
        } catch (Exception ex) {
            return ResponseEntity.status(400).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/{ruleId}/expand")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> expandRule(@PathVariable UUID ruleId, @RequestParam String start, @RequestParam String end) {
        try {
            LocalDate s = LocalDate.parse(start);
            LocalDate e = LocalDate.parse(end);
            var created = expansionService.expandRule(ruleId, s, e);
            return ResponseEntity.ok(Map.of("createdCount", created.size(), "created", created));
        } catch (Exception ex) {
            return ResponseEntity.status(400).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/expandAll")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> expandAll(@RequestParam String start, @RequestParam String end) {
        try {
            LocalDate s = LocalDate.parse(start);
            LocalDate e = LocalDate.parse(end);
            var created = expansionService.expandAll(s, e);
            return ResponseEntity.ok(Map.of("createdCount", created.size(), "created", created));
        } catch (Exception ex) {
            return ResponseEntity.status(400).body(Map.of("error", ex.getMessage()));
        }
    }
}
