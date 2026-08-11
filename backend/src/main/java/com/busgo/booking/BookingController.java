package com.busgo.booking;

import com.busgo.booking.dto.BookingResponse;
import com.busgo.booking.dto.ConfirmRequest;
import com.busgo.booking.dto.HoldRequest;
import com.busgo.booking.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final SeatLockService seatLockService;
    private final BookingService bookingService;

    public BookingController(SeatLockService seatLockService, BookingService bookingService) {
        this.seatLockService = seatLockService;
        this.bookingService = bookingService;
    }

    @PostMapping("/hold")
    public ResponseEntity<?> hold(@RequestBody HoldRequest req) {
        if (req.getTripId() == null || req.getSeatCodes() == null || req.getSeatCodes().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "tripId and seatCodes required"));
        }
        SeatLockService.HoldResult result = seatLockService.holdSeats(req.getTripId(), req.getSeatCodes(), req.getUserId());
        if (result.holdToken() == null) {
            return ResponseEntity.status(409).body(Map.of("success", false, "message", "One or more seats are already held", "conflicts", result.conflicts()));
        }
        return ResponseEntity.ok(Map.of("holdToken", result.holdToken()));
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestBody ConfirmRequest req) {
        if (req.getHoldToken() == null) return ResponseEntity.badRequest().body(Map.of("success", false, "message", "holdToken required"));
        try {
            // For Option A, paymentReference is required
            if (req.getPaymentReference() == null || req.getPaymentReference().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "paymentReference required"));
            }
            // In a real app we'd derive userId from the authenticated principal; for now accept a placeholder userId from the hold mapping or require it elsewhere.
            // Here we'll create booking with a random user id placeholder until integration with auth/user is done.
            java.util.UUID fakeUser = UUID.randomUUID();
            var booking = bookingService.confirmHoldAndCreateBooking(req.getHoldToken(), req.getPaymentReference(), fakeUser);
            BookingResponse resp = new BookingResponse(booking.getId().toString(), booking.getStatus());
            return ResponseEntity.ok(resp);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(400).body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    @PostMapping("/cancel")
    public ResponseEntity<?> cancel(@RequestBody ConfirmRequest req) {
        if (req.getHoldToken() == null) return ResponseEntity.badRequest().body(Map.of("success", false, "message", "holdToken required"));
        seatLockService.releaseHold(req.getHoldToken());
        return ResponseEntity.ok(Map.of("success", true));
    }
}
