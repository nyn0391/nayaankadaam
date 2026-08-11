package com.busgo.trip;

import com.busgo.booking.model.TripSeat;
import com.busgo.booking.repo.TripSeatRepository;
import com.busgo.bus.BusRepository;
import com.busgo.layout.SeatLayoutRepository;
import com.busgo.route.RouteRepository;
import com.busgo.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/templates")
public class TripTemplateController {

    private final TripTemplateRepository templateRepository;
    private final TripInstanceRepository instanceRepository;
    private final BusRepository busRepository;
    private final SeatLayoutRepository seatLayoutRepository;
    private final TripSeatRepository tripSeatRepository;
    private final SecurityUtils securityUtils;

    public TripTemplateController(TripTemplateRepository templateRepository,
                                  TripInstanceRepository instanceRepository,
                                  BusRepository busRepository,
                                  SeatLayoutRepository seatLayoutRepository,
                                  TripSeatRepository tripSeatRepository,
                                  SecurityUtils securityUtils) {
        this.templateRepository = templateRepository;
        this.instanceRepository = instanceRepository;
        this.busRepository = busRepository;
        this.seatLayoutRepository = seatLayoutRepository;
        this.tripSeatRepository = tripSeatRepository;
        this.securityUtils = securityUtils;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createTemplate(@RequestBody Map<String,Object> body) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        try {
            TripTemplate t = new TripTemplate();
            if (body.containsKey("id")) t.setId(UUID.fromString((String) body.get("id"))); else t.setId(UUID.randomUUID());
            t.setName((String) body.getOrDefault("name", null));
            if (body.containsKey("routeId")) t.setRouteId(UUID.fromString((String) body.get("routeId")));
            if (body.containsKey("busId")) t.setBusId(UUID.fromString((String) body.get("busId")));
            if (body.containsKey("basePrice")) t.setBasePrice(((Number) body.get("basePrice")).doubleValue());
            t.setNotes((String) body.getOrDefault("notes", null));
            var uid = securityUtils.getUserId(auth);
            if (uid != null) t.setCreatedBy(uid);
            templateRepository.save(t);
            return ResponseEntity.ok(t);
        } catch (Exception ex) {
            return ResponseEntity.status(400).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/{id}/instances")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createInstance(@PathVariable UUID id, @RequestBody Map<String,Object> body) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        try {
            var template = templateRepository.findById(id).orElseThrow();
            // expected: departureAt (ISO timestamptz), optional instancePrice, timezone
            String departure = (String) body.get("departureAt");
            String timezone = (String) body.getOrDefault("timezone", "UTC");
            Double instancePrice = body.containsKey("instancePrice") ? ((Number) body.get("instancePrice")).doubleValue() : null;

            OffsetDateTime odt = OffsetDateTime.parse(departure);

            TripInstance inst = new TripInstance();
            inst.setId(UUID.randomUUID());
            inst.setTemplateId(template.getId());
            inst.setDepartureAt(odt);
            inst.setInstancePrice(instancePrice);
            inst.setTimezone(timezone);
            var uid = securityUtils.getUserId(auth);
            if (uid != null) inst.setCreatedBy(uid);
            instanceRepository.save(inst);

            // generate trip seats from bus' seat layout
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
                                        com.busgo.booking.model.TripSeat ts = new com.busgo.booking.model.TripSeat();
                                        ts.setId(UUID.randomUUID());
                                        ts.setTripInstanceId(inst.getId());
                                        ts.setSeatCode(seatCode);
                                        ts.setIsBooked(false);
                                        if (s.has("price")) ts.setPrice(java.math.BigDecimal.valueOf(s.get("price").asDouble()));
                                        tripSeatRepository.save(ts);
                                    }
                                }
                            } catch (Exception ex) {
                                // ignore
                            }
                        });
                    }
                });
            }

            return ResponseEntity.ok(Map.of("instanceId", inst.getId().toString()));
        } catch (Exception ex) {
            return ResponseEntity.status(400).body(Map.of("error", ex.getMessage()));
        }
    }
}
