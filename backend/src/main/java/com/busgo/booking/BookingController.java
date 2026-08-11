package com.busgo.booking;

import com.busgo.booking.dto.BookingResponse;
import com.busgo.booking.dto.ConfirmRequest;
import com.busgo.booking.dto.HoldRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final SeatLockService seatLockService;

    public BookingController(SeatLockService seatLockService) {
        this.seatLockService = seatLockService;
    }

    @PostMapping("/hold")
    public ResponseEntity<?> hold(@RequestBody HoldRequest req) {
        if (req.getTripId() == null || req.getSeatCodes() == null || req.getSeatCodes().isEmpty()) {
            return ResponseEntity.badRequest().body("tripId and seatCodes required")
        }
        String token = seatLockService.holdSeats(req.getTripId(), req.getSeatCodes(), req.getUserId());
        if (token == null) return ResponseEntity.status(409).body("One or more seats are already held") ;
        return ResponseEntity.ok(Map.of("holdToken", token));
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestBody ConfirmRequest req) {
        if (req.getHoldToken() == null) return ResponseEntity.badRequest().body("holdToken required");
        boolean ok = seatLockService.confirmHold(req.getHoldToken());
        if (!ok) return ResponseEntity.status(404).body("Hold not found or expired");
        // In a real implementation we'd create booking records and payment here
        BookingResponse resp = new BookingResponse(java.util.UUID.randomUUID().toString(), "CONFIRMED");
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/cancel")
    public ResponseEntity<?> cancel(@RequestBody ConfirmRequest req) {
        if (req.getHoldToken() == null) return ResponseEntity.badRequest().body("holdToken required");
        seatLockService.releaseHold(req.getHoldToken());
        return ResponseEntity.ok(Map.of("success", true));
    }
}
