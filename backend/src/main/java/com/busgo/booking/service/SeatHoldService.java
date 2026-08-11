package com.busgo.booking.service;

import com.busgo.booking.SeatHold;
import com.busgo.booking.repo.SeatHoldRepository;
import com.busgo.booking.repo.TripSeatRepository;
import com.busgo.booking.model.TripSeat;
import com.busgo.booking.repo.BookingRepository;
import com.busgo.booking.model.Booking;
import com.busgo.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class SeatHoldService {

    private final StringRedisTemplate redis;
    private final SeatHoldRepository seatHoldRepository;
    private final TripSeatRepository tripSeatRepository;
    private final BookingRepository bookingRepository;
    private final SecurityUtils securityUtils;

    private final Duration defaultTtl;
    private final int maxSeatsPerHold;

    public SeatHoldService(StringRedisTemplate redis,
                           SeatHoldRepository seatHoldRepository,
                           TripSeatRepository tripSeatRepository,
                           BookingRepository bookingRepository,
                           SecurityUtils securityUtils,
                           @Value("${app.hold.ttl.seconds:600}") long holdTtlSeconds,
                           @Value("${app.hold.maxSeats:6}") int maxSeatsPerHold) {
        this.redis = redis;
        this.seatHoldRepository = seatHoldRepository;
        this.tripSeatRepository = tripSeatRepository;
        this.bookingRepository = bookingRepository;
        this.securityUtils = securityUtils;
        this.defaultTtl = Duration.ofSeconds(holdTtlSeconds);
        this.maxSeatsPerHold = maxSeatsPerHold;
    }

    private String redisKey(UUID instanceId, String seatCode) {
        return String.format("seat:hold:%s:%s", instanceId.toString(), seatCode);
    }

    public static class HoldResult {
        public String holdToken;
        public OffsetDateTime expiresAt;
        public List<String> seats;
    }

    public static class ConfirmResult {
        public UUID bookingId;
        public BigDecimal amount;
    }

    @Transactional
    public HoldResult createHold(UUID instanceId, List<String> seatCodes, Integer holdSeconds) {
        if (seatCodes == null || seatCodes.isEmpty()) throw new IllegalArgumentException("no seats");
        if (seatCodes.size() > maxSeatsPerHold) throw new IllegalArgumentException("exceeds max seats per hold: " + maxSeatsPerHold);
        long ttl = (holdSeconds == null) ? defaultTtl.getSeconds() : holdSeconds;
        if (ttl <= 0) ttl = defaultTtl.getSeconds();

        String holdToken = UUID.randomUUID().toString();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusSeconds(ttl);
        UUID userId = securityUtils.getCurrentUserId().orElse(null);

        List<String> lockedKeys = new ArrayList<>();
        try {
            // Acquire redis locks for each seat (fast path)
            for (String seat : seatCodes) {
                String key = redisKey(instanceId, seat);
                Boolean ok = redis.opsForValue().setIfAbsent(key, holdToken, ttl, TimeUnit.SECONDS);
                if (ok == null || !ok) {
                    // failed to lock
                    throw new IllegalStateException("seat not available: " + seat);
                }
                lockedKeys.add(key);
            }

            // double-check DB there is no booking and no active hold (safety)
            for (String seat : seatCodes) {
                List<SeatHold> existing = seatHoldRepository.findByTripInstanceIdAndSeatCodeAndExpiresAtAfter(instanceId, seat, OffsetDateTime.now());
                if (!existing.isEmpty()) throw new IllegalStateException("seat already held: " + seat);
                Optional<TripSeat> tsOpt = tripSeatRepository.findByTripInstanceIdAndSeatCode(instanceId, seat);
                if (tsOpt.isPresent() && Boolean.TRUE.equals(tsOpt.get().getIsBooked())) throw new IllegalStateException("seat already booked: " + seat);
            }

            // persist seat_holds
            for (String seat : seatCodes) {
                SeatHold sh = new SeatHold();
                sh.setId(UUID.randomUUID());
                sh.setTripInstanceId(instanceId);
                sh.setSeatCode(seat);
                sh.setUserId(userId);
                sh.setHoldToken(holdToken);
                sh.setCreatedAt(OffsetDateTime.now());
                sh.setExpiresAt(expiresAt);
                seatHoldRepository.save(sh);
            }

            HoldResult r = new HoldResult();
            r.holdToken = holdToken;
            r.expiresAt = expiresAt;
            r.seats = seatCodes;
            return r;
        } catch (RuntimeException ex) {
            // cleanup redis keys
            for (String k : lockedKeys) redis.delete(k);
            throw ex;
        }
    }

    @Transactional
    public void releaseHold(String holdToken) {
        List<SeatHold> holds = seatHoldRepository.findByHoldToken(holdToken);
        for (SeatHold sh : holds) {
            String key = redisKey(sh.getTripInstanceId(), sh.getSeatCode());
            redis.delete(key);
        }
        seatHoldRepository.deleteByHoldToken(holdToken);
    }

    @Transactional
    public HoldResult extendHold(String holdToken, Integer extraSeconds) {
        List<SeatHold> holds = seatHoldRepository.findByHoldToken(holdToken);
        if (holds.isEmpty()) throw new IllegalArgumentException("hold not found");
        long extra = (extraSeconds == null) ? defaultTtl.getSeconds() : extraSeconds;
        OffsetDateTime newExpiry = OffsetDateTime.now().plusSeconds(extra);
        for (SeatHold sh : holds) {
            String key = redisKey(sh.getTripInstanceId(), sh.getSeatCode());
            Boolean ok = redis.expire(key, extra, TimeUnit.SECONDS);
            if (ok == null || !ok) throw new IllegalStateException("failed to extend hold in redis");
            sh.setExpiresAt(newExpiry);
            seatHoldRepository.save(sh);
        }
        HoldResult r = new HoldResult();
        r.holdToken = holdToken;
        r.expiresAt = newExpiry;
        r.seats = new ArrayList<>();
        holds.forEach(h -> r.seats.add(h.getSeatCode()));
        return r;
    }

    @Transactional
    public ConfirmResult confirmBooking(String holdToken, Map<String,Object> paymentInfo) {
        // Find holds
        List<SeatHold> holds = seatHoldRepository.findByHoldToken(holdToken);
        if (holds.isEmpty()) throw new IllegalArgumentException("hold not found");
        UUID instanceId = holds.get(0).getTripInstanceId();

        // Create booking
        Booking booking = new Booking();
        booking.setId(UUID.randomUUID());
        booking.setTripInstanceId(instanceId);
        booking.setUserId(securityUtils.getCurrentUserId().orElse(null));
        booking.setStatus("CONFIRMED");
        booking.setCreatedAt(OffsetDateTime.now());

        BigDecimal total = BigDecimal.ZERO;
        // Lock and mark seats
        for (SeatHold sh : holds) {
            TripSeat ts = tripSeatRepository.lockByInstanceIdAndSeatCode(sh.getTripInstanceId(), sh.getSeatCode())
                    .orElseThrow(() -> new IllegalStateException("seat not found: " + sh.getSeatCode()));
            if (Boolean.TRUE.equals(ts.getIsBooked())) throw new IllegalStateException("seat already booked: " + sh.getSeatCode());
            // mark booked
            ts.setIsBooked(true);
            ts.setBookingId(booking.getId());
            tripSeatRepository.save(ts);
            if (ts.getPrice() != null) total = total.add(ts.getPrice());
        }

        booking.setAmount(total);
        bookingRepository.save(booking);

        // delete holds and redis keys
        for (SeatHold sh : holds) {
            String key = redisKey(sh.getTripInstanceId(), sh.getSeatCode());
            redis.delete(key);
        }
        seatHoldRepository.deleteByHoldToken(holdToken);

        ConfirmResult res = new ConfirmResult();
        res.bookingId = booking.getId();
        res.amount = total;
        return res;
    }
}
