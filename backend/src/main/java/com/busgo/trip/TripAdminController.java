package com.busgo.trip;

import com.busgo.booking.model.TripSeat;
import com.busgo.booking.repo.TripSeatRepository;
import com.busgo.bus.BusRepository;
import com.busgo.layout.SeatLayoutRepository;
import com.busgo.route.RouteRepository;
import com.busgo.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/trips")
public class TripAdminController {

    private final TripRepository tripRepository;
    private final BusRepository busRepository;
    private final RouteRepository routeRepository;
    private final SeatLayoutRepository seatLayoutRepository;
    private final TripSeatRepository tripSeatRepository;
    private final SecurityUtils securityUtils;

    public TripAdminController(TripRepository tripRepository,
                               BusRepository busRepository,
                               RouteRepository routeRepository,
                               SeatLayoutRepository seatLayoutRepository,
                               TripSeatRepository tripSeatRepository,
                               SecurityUtils securityUtils) {
        this.tripRepository = tripRepository;
        this.busRepository = busRepository;
        this.routeRepository = routeRepository;
        this.seatLayoutRepository = seatLayoutRepository;
        this.tripSeatRepository = tripSeatRepository;
        this.securityUtils = securityUtils;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createTrip(@RequestBody Map<String, Object> body) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // expected body: id(optional), routeId, busId, scheduledAt, basePrice
        try {
            UUID routeId = UUID.fromString((String) body.get("routeId"));
            UUID busId = UUID.fromString((String) body.get("busId"));
            var route = routeRepository.findById(routeId).orElseThrow();
            var bus = busRepository.findById(busId).orElseThrow();

            Trip trip = new Trip();
            UUID tripId = UUID.randomUUID();
            trip.setId(tripId);
            trip.setRouteId(routeId);
            trip.setBusId(busId);
            trip.setBasePrice(body.getOrDefault("basePrice", 0.0) instanceof Number ? ((Number) body.getOrDefault("basePrice", 0.0)).doubleValue() : Double.parseDouble(body.getOrDefault("basePrice", "0").toString()));
            var uid = securityUtils.getUserId(auth);
            if (uid != null) trip.setCreatedBy(uid);
            tripRepository.save(trip);

            // generate trip seats from seat layout if present
            if (bus.getSeatLayoutId() != null) {
                seatLayoutRepository.findById(bus.getSeatLayoutId()).ifPresent(layout -> {
                    // layout.content expected to be JSON with seats array
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                        var node = om.readTree(layout.getContent());
                        var seats = node.get("seats");
                        if (seats != null && seats.isArray()) {
                            for (var s : seats) {
                                String seatCode = s.get("seatCode").asText();
                                TripSeat ts = new TripSeat();
                                ts.setId(UUID.randomUUID());
                                ts.setTripId(tripId);
                                ts.setSeatCode(seatCode);
                                ts.setIsBooked(false);
                                if (s.has("price")) ts.setPrice(java.math.BigDecimal.valueOf(s.get("price").asDouble()));
                                tripSeatRepository.save(ts);
                            }
                        }
                    } catch (Exception ex) {
                        // ignore; provisioning of seats optional
                    }
                });
            }

            return ResponseEntity.ok(Map.of("tripId", tripId.toString()));
        } catch (Exception ex) {
            return ResponseEntity.status(400).body(Map.of("error", ex.getMessage()));
        }
    }
}
