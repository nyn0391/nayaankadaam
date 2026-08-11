package com.busgo.booking.api;

import com.busgo.booking.service.SeatHoldService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trips")
public class HoldController {

    private final SeatHoldService seatHoldService;

    public HoldController(SeatHoldService seatHoldService) {
        this.seatHoldService = seatHoldService;
    }

    @PostMapping("/{instanceId}/holds")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createHold(@PathVariable UUID instanceId, @RequestBody Map<String,Object> body) {
        try {
            @SuppressWarnings("unchecked")
            List<String> seats = (List<String>) body.get("seats");
            Integer ttl = body.containsKey("holdSeconds") ? (Integer) body.get("holdSeconds") : null;
            var res = seatHoldService.createHold(instanceId, seats, ttl);
            return ResponseEntity.ok(Map.of("holdToken", res.holdToken, "expiresAt", res.expiresAt.toString(), "seats", res.seats));
        } catch (Exception ex) {
            return ResponseEntity.status(400).body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/{instanceId}/holds/{holdToken}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> releaseHold(@PathVariable UUID instanceId, @PathVariable String holdToken) {
        try {
            seatHoldService.releaseHold(holdToken);
            return ResponseEntity.ok(Map.of("released", true));
        } catch (Exception ex) {
            return ResponseEntity.status(400).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/{instanceId}/holds/{holdToken}/extend")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> extendHold(@PathVariable UUID instanceId, @PathVariable String holdToken, @RequestBody Map<String,Object> body) {
        try {
            Integer extra = body.containsKey("extraSeconds") ? (Integer) body.get("extraSeconds") : null;
            var res = seatHoldService.extendHold(holdToken, extra);
            return ResponseEntity.ok(Map.of("holdToken", res.holdToken, "expiresAt", res.expiresAt.toString(), "seats", res.seats));
        } catch (Exception ex) {
            return ResponseEntity.status(400).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/holds/{holdToken}/confirm")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> confirmHold(@PathVariable String holdToken, @RequestBody Map<String,Object> body) {
        try {
            var res = seatHoldService.confirmBooking(holdToken, body);
            return ResponseEntity.ok(Map.of("bookingId", res.bookingId, "amount", res.amount));
        } catch (Exception ex) {
            return ResponseEntity.status(400).body(Map.of("error", ex.getMessage()));
        }
    }
}
