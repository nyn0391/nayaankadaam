package com.busgo.trip;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class ScheduleExpansionScheduler {

    private final ScheduleExpansionService expansionService;

    public ScheduleExpansionScheduler(ScheduleExpansionService expansionService) {
        this.expansionService = expansionService;
    }

    // Run daily at 02:00 UTC to expand schedules for the next 30 days
    @Scheduled(cron = "0 0 2 * * *", zone = "UTC")
    public void expandNext30Days() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(30);
        List<java.util.UUID> created = expansionService.expandAll(start, end);
        // no-op logging for now; could be enhanced to emit metrics or audit entries
        System.out.println("ScheduleExpansionScheduler: created instances=" + created.size());
    }
}
