package com.busgo.booking.api;

import com.busgo.booking.model.TripSeat;
import com.busgo.booking.repo.TripSeatRepository;
import com.busgo.bus.BusRepository;
import com.busgo.layout.SeatLayoutRepository;
import com.busgo.trip.TripInstanceRepository;
import com.busgo.trip.TripTemplateRepository;
import com.busgo.booking.repo.SeatHoldRepository;
import com.busgo.booking.SeatHold;
import com.busgo.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/trips")
public class TripSeatMapController {

    private final TripSeatRepository tripSeatRepository;
    private final TripInstanceRepository tripInstanceRepository;
    private final TripTemplateRepository tripTemplateRepository;
    private final BusRepository busRepository;
    private final SeatLayoutRepository seatLayoutRepository;
    private final SeatHoldRepository seatHoldRepository;
    private final SecurityUtils securityUtils;

    public TripSeatMapController(TripSeatRepository tripSeatRepository,
                                 TripInstanceRepository tripInstanceRepository,
                                 TripTemplateRepository tripTemplateRepository,
                                 BusRepository busRepository,
                                 SeatLayoutRepository seatLayoutRepository,
                                 SeatHoldRepository seatHoldRepository,
                                 SecurityUtils securityUtils) {
        this.tripSeatRepository = tripSeatRepository;
        this.tripInstanceRepository = tripInstanceRepository;
        this.tripTemplateRepository = tripTemplateRepository;
        this.busRepository = busRepository;
        this.seatLayoutRepository = seatLayoutRepository;
        this.seatHoldRepository = seatHoldRepository;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/{instanceId}/seatmap")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getSeatMap(@PathVariable UUID instanceId) {
        var instanceOpt = tripInstanceRepository.findById(instanceId);
        if (instanceOpt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error","instance not found"));
        var instance = instanceOpt.get();
        Map<String,Object> layoutObj = Map.of();
        // try to load layout content
        if (instance.getTemplateId() != null) {
            tripTemplateRepository.findById(instance.getTemplateId()).ifPresent(t -> {
                if (t.getBusId() != null) {
                    busRepository.findById(t.getBusId()).ifPresent(b -> {
                        if (b.getSeatLayoutId() != null) {
                            seatLayoutRepository.findById(b.getSeatLayoutId()).ifPresent(sl -> {
                                try {
                                    com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                                    var node = om.readTree(sl.getContent());
                                    layoutObj.put("content", om.readTree(sl.getContent()));
                                } catch (Exception ignored) {}
                            });
                        }
                    });
                }
            });
        }

        // load seats
        List<TripSeat> seats = tripSeatRepository.findByTripInstanceId(instanceId);
        // load active holds for this instance
        OffsetDateTime now = OffsetDateTime.now();
        Map<String, Map<String,Object>> holdsBySeat = new HashMap<>();
        for (TripSeat ts : seats) {
            List<SeatHold> active = seatHoldRepository.findByTripInstanceIdAndSeatCodeAndExpiresAtAfter(instanceId, ts.getSeatCode(), now);
            if (!active.isEmpty()) {
                // pick earliest expiry
                SeatHold sh = active.get(0);
                Map<String,Object> info = new HashMap<>();
                info.put("expiresAt", sh.getExpiresAt());
                info.put("heldByMe", false);
                // determine if held by current user
                var uid = securityUtils.getCurrentUserId().orElse(null);
                if (sh.getUserId() != null && sh.getUserId().equals(uid)) info.put("heldByMe", true);
                holdsBySeat.put(ts.getSeatCode(), info);
            }
        }

        // build response seats
        List<Map<String,Object>> seatDtos = seats.stream().map(s -> {
            Map<String,Object> m = new HashMap<>();
            m.put("seatCode", s.getSeatCode());
            m.put("isBooked", s.getIsBooked());
            m.put("bookingId", s.getBookingId());
            if (holdsBySeat.containsKey(s.getSeatCode())) {
                m.put("held", true);
                m.putAll(holdsBySeat.get(s.getSeatCode()));
            } else {
                m.put("held", false);
            }
            return m;
        }).collect(Collectors.toList());

        Map<String,Object> resp = new HashMap<>();
        resp.put("layout", layoutObj.getOrDefault("content", Map.of()));
        resp.put("seats", seatDtos);
        return ResponseEntity.ok(resp);
    }
}
