package com.busgo.booking;

import com.busgo.booking.dto.BookingResponse;
import com.busgo.booking.dto.ConfirmRequest;
import com.busgo.booking.dto.HoldRequest;
import com.busgo.booking.service.BookingService;
import com.busgo.user.User;
import com.busgo.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final SeatLockService seatLockService;
    private final BookingService bookingService;
    private final UserRepository userRepository;

    public BookingController(SeatLockService seatLockService, BookingService bookingService, UserRepository userRepository) {
        this.seatLockService = seatLockService;
        this.bookingService = bookingService;
        this.userRepository = userRepository;
    }

    @PostMapping("/hold")
    public ResponseEntity<?> hold(@RequestBody HoldRequest req) {
        if (req.getTripId() == null || req.getSeatCodes() == null || req.getSeatCodes().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "tripId and seatCodes required"));
        }
        // derive userId from authenticated principal
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String principal = auth != null ? auth.getName() : null;
        UUID userId = null;
        if (principal != null) {
            User user = userRepository.findByEmail(principal).orElseGet(() -> userRepository.findByMobile(principal).orElse(null));
            if (user != null) userId = user.getId();
        }

        String userIdStr = userId != null ? userId.toString() : req.getUserId();

        SeatLockService.HoldResult result = seatLockService.holdSeats(req.getTripId(), req.getSeatCodes(), userIdStr);
        if (result.holdToken() == null) {
            return ResponseEntity.status(409).body(Map.of("success", false, "message", "One or more seats are already held", "conflicts", result.conflicts()));
        }
        return ResponseEntity.ok(Map.of("holdToken", result.holdToken(), "expiresIn", result.expiresInSeconds()));
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestBody ConfirmRequest req) {
        if (req.getHoldToken() == null) return ResponseEntity.badRequest().body(Map.of("success", false, "message", "holdToken required"));
        try {
            if (req.getPaymentReference() == null || req.getPaymentReference().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "paymentReference required"));
            }
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String principal = auth != null ? auth.getName() : null;
            UUID userUuid = null;
            if (principal != null) {
                User user = userRepository.findByEmail(principal).orElseGet(() -> userRepository.findByMobile(principal).orElse(null));
                if (user != null) userUuid = user.getId();
            }
            if (userUuid == null) return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthenticated"));

            var booking = bookingService.confirmHoldAndCreateBooking(req.getHoldToken(), req.getPaymentReference(), userUuid);
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
