package com.busgo.trip;

import com.busgo.booking.repo.TripSeatRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/trips")
public class TripSeatController {

    private final TripSeatRepository tripSeatRepository;

    public TripSeatController(TripSeatRepository tripSeatRepository) {
        this.tripSeatRepository = tripSeatRepository;
    }

    @GetMapping("/{tripId}/seats")
    public ResponseEntity<?> getSeats(@PathVariable String tripId) {
        UUID tid;
        try { tid = UUID.fromString(tripId); } catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("success", false, "message", "invalid tripId")); }
        List<Map<String, Object>> seats = tripSeatRepository.findAll().stream()
                .filter(s -> tid.equals(s.getTripId()))
                .map(s -> Map.<String,Object>of(
                        "seatCode", s.getSeatCode(),
                        "isBooked", s.getIsBooked(),
                        "price", s.getPrice()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(seats);
    }
}
